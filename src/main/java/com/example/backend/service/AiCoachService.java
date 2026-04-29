package com.example.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.backend.dto.AiAnalyzeResponseDTO;
import com.example.backend.dto.AiAbCompareResponseDTO;
import com.example.backend.dto.AiAbTopicCompareDTO;
import com.example.backend.dto.AiChatResponseDTO;
import com.example.backend.dto.AiStudyPlanResponseDTO;
import com.example.backend.dto.AiStudyTaskDTO;
import com.example.backend.dto.AiTrainingRowDTO;
import com.example.backend.dto.AiWeakTopicDTO;
import com.example.backend.model.AppUser;
import com.example.backend.repository.CevapRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AiCoachService {

    private final CevapRepository cevapRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${ai.ml.enabled:false}")
    private boolean mlEnabled;

    @Value("${ai.ml.base-url:http://localhost:8001}")
    private String mlBaseUrl;

    public AiCoachService(CevapRepository cevapRepository, ObjectMapper objectMapper) {
        this.cevapRepository = cevapRepository;
        this.objectMapper = objectMapper;
    }

    public AiAnalyzeResponseDTO analyzeWeakTopics(AppUser user, int days, int limit) {
        int safeDays = Math.max(7, Math.min(days, 180));
        int safeLimit = Math.max(1, Math.min(limit, 20));
        Instant from = Instant.now().minus(safeDays, ChronoUnit.DAYS);

        List<Object[]> rows = cevapRepository.findTopicPerformanceForUser(user.getId(), from);
        List<TopicStat> stats = toTopicStats(rows);
        List<AiWeakTopicDTO> all = toWeakTopicList(stats);
        List<AiWeakTopicDTO> weak = all.stream()
                .sorted(Comparator.comparingDouble(AiWeakTopicDTO::riskScore).reversed())
                .limit(safeLimit)
                .toList();

        int totalAnswers = all.stream().mapToInt(AiWeakTopicDTO::totalCount).sum();
        int totalCorrect = all.stream().mapToInt(AiWeakTopicDTO::correctCount).sum();
        double overallSuccessRate = totalAnswers > 0 ? round2((totalCorrect * 100.0) / totalAnswers) : 0.0;

        List<String> focusTips = buildFocusTips(weak, overallSuccessRate);
        return new AiAnalyzeResponseDTO(safeDays, totalAnswers, overallSuccessRate, weak, focusTips);
    }

    public AiStudyPlanResponseDTO suggestStudyPlan(AppUser user, int days, int dailyMinutes, String mode) {
        int safeMinutes = Math.max(30, Math.min(dailyMinutes, 360));
        String safeMode = normalizeMode(mode);

        AiAnalyzeResponseDTO analysis = analyzeWeakTopics(user, days, 8);
        List<AiStudyTaskDTO> tasks = new ArrayList<>();
        /*
         * Gunluk hedef dakika (safeMinutes) kullanicinin her musait gune yaymak istedigi ust sudur.
         * Gorev listesi tek gunluk degil; haftalik takvimde gun basina ~safeMinutes dolsun diye
         * yaklasik 7 gunluk toplam sure kadar gorev uretilir (frontend gunluk cap ile dagitir).
         */
        final int daysPerWeek = 7;
        int weeklyBudget = Math.min(safeMinutes * daysPerWeek, safeMinutes * 31);
        int remaining = weeklyBudget;
        int priority = 1;
        final int maxTasks = 220;

        List<AiWeakTopicDTO> weak = analysis.weakTopics();
        if (weak.isEmpty()) {
            while (remaining > 0 && tasks.size() < maxTasks) {
                int chunk = Math.min(remaining, safeMinutes);
                tasks.add(new AiStudyTaskDTO(
                        "review",
                        "Genel tekrar blogu",
                        "Son cozdugun konulardan karisik soru seti veya ozet tekrar yap.",
                        chunk,
                        priority++
                ));
                remaining -= chunk;
            }
        } else {
            int wi = 0;
            int noProgressRounds = 0;
            while (remaining > 0 && tasks.size() < maxTasks) {
                AiWeakTopicDTO t = weak.get(wi % weak.size());
                wi++;

                int before = remaining;

                if (!"test".equals(safeMode) && remaining >= 12) {
                    int videoMinutes = Math.min(Math.max(12, safeMinutes / 6), remaining);
                    tasks.add(new AiStudyTaskDTO(
                            "video",
                            t.konuAd() + " konu videosu",
                            t.dersAd() + " > " + t.konuAd() + " icin hedefli tekrar videosu izle.",
                            videoMinutes,
                            priority++
                    ));
                    remaining -= videoMinutes;
                }

                if (remaining <= 0) break;

                if (!"video".equals(safeMode) && remaining >= 15) {
                    int testMinutes = Math.min(Math.max(15, safeMinutes / 5), remaining);
                    tasks.add(new AiStudyTaskDTO(
                            "quiz",
                            t.konuAd() + " test seti",
                            "Bu konuda 20-30 soru coz ve yanlislari not al.",
                            testMinutes,
                            priority++
                    ));
                    remaining -= testMinutes;
                }

                if (remaining == before) {
                    noProgressRounds++;
                    if (noProgressRounds >= weak.size() * 3) {
                        break;
                    }
                } else {
                    noProgressRounds = 0;
                }
            }

            if (remaining > 0) {
                tasks.add(new AiStudyTaskDTO(
                        "review",
                        "Gun sonu tekrar",
                        "Bu hafta zorlandigin konulardan kisa bir tekrar blogu.",
                        remaining,
                        priority
                ));
            }
        }

        String summary = buildPlanSummary(analysis, safeMinutes, safeMode);
        return new AiStudyPlanResponseDTO(analysis.analyzedDays(), safeMinutes, safeMode, tasks, summary);
    }

    public AiChatResponseDTO chat(AppUser user, String message) {
        String raw = String.valueOf(message == null ? "" : message).trim();
        String msg = raw.toLowerCase(Locale.forLanguageTag("tr-TR"));

        AiAnalyzeResponseDTO analysis = analyzeWeakTopics(user, 30, 8);
        AiStudyPlanResponseDTO plan = suggestStudyPlan(user, 30, 120, "mixed");

        String answer;
        if (matchesDenemeAnalysis(msg)) {
            answer = buildDenemeAnalysisAnswer(analysis);
        } else if (matchesQuick30Plan(msg)) {
            AiStudyPlanResponseDTO quick = suggestStudyPlan(user, 30, 30, "mixed");
            answer = buildQuick30FullAnswer(quick, analysis);
        } else if (matchesWeakTopics(msg)) {
            answer = buildComprehensiveWeakTopicsAnswer(analysis);
        } else if (matchesProgramOrToday(msg)) {
            answer = buildComprehensivePlanAnswer(plan, analysis);
        } else if (msg.contains("net") || msg.contains("basari") || msg.contains("puan")) {
            answer = buildPerformanceAnswer(analysis);
        } else if (msg.contains("merhaba") || msg.contains("selam") || msg.contains("yardim")) {
            answer = buildWelcomeAnswer(analysis);
        } else {
            answer = buildDefaultGuidanceAnswer(analysis);
        }

        return new AiChatResponseDTO(answer, defaultQuickReplies());
    }

    private static boolean matchesDenemeAnalysis(String msg) {
        return msg.contains("deneme")
                || (msg.contains("analiz") && (msg.contains("sinav") || msg.contains("sınav") || msg.contains("sinavi") || msg.contains("sınavı")))
                || msg.contains("deneme analizi")
                || msg.contains("son deneme");
    }

    private static boolean matchesQuick30Plan(String msg) {
        boolean has30 = msg.contains("30") || msg.contains("otuz");
        boolean timeWord = msg.contains("dakika") || msg.contains("dk") || msg.contains("dak");
        return (has30 && timeWord)
                || msg.contains("hizli plan") || msg.contains("hızlı plan")
                || msg.contains("30 dk") || msg.contains("otuz dakika")
                || msg.contains("kisa plan") || msg.contains("kısa plan");
    }

    private static boolean matchesWeakTopics(String msg) {
        return msg.contains("eksik") || msg.contains("zayif") || msg.contains("zayıf")
                || msg.contains("konu") && (msg.contains("hangi") || msg.contains("nerede") || msg.contains("liste"))
                || msg.contains("eksiklerim");
    }

    private static boolean matchesProgramOrToday(String msg) {
        return msg.contains("program") || msg.contains("plan") || msg.contains("calis") || msg.contains("çalış")
                || msg.contains("bugun") || msg.contains("bugün") || msg.contains("ne calis") || msg.contains("ne çalış");
    }

    private static List<String> defaultQuickReplies() {
        return List.of(
                "Eksiklerim neler?",
                "30 dakikada hizli plan hazirla",
                "Deneme analizi yap",
                "Bugun calisma programi olustur",
                "Genel basari ve net durumum",
                "Hangi konuda en zayifim?"
        );
    }

    private String buildDenemeAnalysisAnswer(AiAnalyzeResponseDTO analysis) {
        if (analysis.weakTopics().isEmpty()) {
            return "Deneme / genel performans analizi icin henuz yeterli veri yok. "
                    + "Once en az bir deneme veya soru cozumu tamamladiginda burada konu bazli geri donusum plani goreceksin.\n\n"
                    + "Oneri: Haftada en az 1 tam deneme + gunluk 20 soru ile veri biriktir.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Deneme ve son donem performansina gore ozet analiz:\n\n");
        sb.append("• Genel basari: %").append(analysis.overallSuccessRate())
                .append(" (son ").append(analysis.analyzedDays()).append(" gun, toplam ")
                .append(analysis.totalAnswers()).append(" cevap).\n\n");
        sb.append("• Oncelikli tekrar konulari (deneme sonrasi odak):\n");
        int n = Math.min(5, analysis.weakTopics().size());
        for (int i = 0; i < n; i++) {
            AiWeakTopicDTO t = analysis.weakTopics().get(i);
            sb.append("  ").append(i + 1).append(") ").append(t.dersAd()).append(" / ").append(t.konuAd())
                    .append(" — risk %").append(t.riskScore()).append(", basari %").append(t.successRate()).append("\n");
            sb.append("     -> ").append(t.recommendation()).append("\n");
        }
        if (!analysis.focusTips().isEmpty()) {
            sb.append("\n• Ek ipuclari:\n");
            for (String tip : analysis.focusTips()) {
                sb.append("  - ").append(tip).append("\n");
            }
        }
        sb.append("\nDeneme sonrasi rutin: 1) Yanlislarin cozumunu izle veya not al, 2) Ayni konudan 10 soru tekrar, 3) Ertesi gun kisa bir tekrar testi.");
        return sb.toString();
    }

    private String buildQuick30FullAnswer(AiStudyPlanResponseDTO quick, AiAnalyzeResponseDTO analysis) {
        if (quick.tasks().isEmpty()) {
            return "30 dakikalik plan icin yeterli veri yok. Once kisa bir quiz veya deneme coz.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("30 dakikalik yogun plan (son ").append(analysis.analyzedDays()).append(" gun verisine gore):\n\n");
        sb.append(quick.summary()).append("\n\n");
        sb.append("Adim adim (toplam ~").append(quick.dailyMinutes()).append(" dk):\n");
        int step = 1;
        for (AiStudyTaskDTO t : quick.tasks()) {
            sb.append(step++).append(". ").append(t.title()).append(" (" + t.estimatedMinutes() + " dk)\n");
            sb.append("   ").append(t.description()).append("\n");
        }
        sb.append("\nIpucu: Sureyi asarsan sonraki gune en zor 2 maddeyi tasiyabilirsin.");
        return sb.toString();
    }

    private String buildComprehensiveWeakTopicsAnswer(AiAnalyzeResponseDTO analysis) {
        if (analysis.weakTopics().isEmpty()) {
            return "Eksik konu listesi icin henuz yeterli veri yok. "
                    + "Daha fazla soru cozdukce risk skorlari ve oneriler netlesir.\n\n"
                    + "Oneri: Haftada en az 1 deneme + gunluk hedefli soru cozumu yap.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Eksiklerin (oncelik sirasiyla):\n\n");
        int limit = Math.min(6, analysis.weakTopics().size());
        for (int i = 0; i < limit; i++) {
            AiWeakTopicDTO t = analysis.weakTopics().get(i);
            sb.append(i + 1).append(". ").append(t.dersAd()).append(" / ").append(t.konuAd()).append("\n");
            sb.append("   Risk: %").append(t.riskScore()).append(", basari: %").append(t.successRate())
                    .append(", bos: ").append(t.blankCount()).append(", yanlis: ").append(t.wrongCount()).append("\n");
            sb.append("   Oneri: ").append(t.recommendation());
            if (t.modelVersion() != null && !t.modelVersion().isBlank()) {
                sb.append(" (kaynak: ").append(t.source()).append(", ").append(t.modelVersion()).append(")");
            }
            sb.append("\n\n");
        }
        sb.append("Genel ortalama basari: %").append(analysis.overallSuccessRate())
                .append(". Bu konularda once konu ozeti + sonra kisa test seti onerilir.");
        return sb.toString();
    }

    private String buildComprehensivePlanAnswer(AiStudyPlanResponseDTO plan, AiAnalyzeResponseDTO analysis) {
        if (plan.tasks().isEmpty()) {
            return "Bugun icin detayli program olusturulamadi. Once en az bir oturum (quiz/deneme) tamamla.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Kisisel calisma programi (gunluk ").append(plan.dailyMinutes()).append(" dk, mod: ")
                .append(plan.mode()).append("):\n\n");
        sb.append(plan.summary()).append("\n\n");
        sb.append("Gorevler:\n");
        int i = 1;
        for (AiStudyTaskDTO t : plan.tasks()) {
            sb.append(i++).append(". [").append(t.taskType()).append("] ").append(t.title())
                    .append(" — ").append(t.estimatedMinutes()).append(" dk\n");
            sb.append("   ").append(t.description()).append("\n");
        }
        sb.append("\nBu programi kaydederek sonra tekrar acabilirsin (uygulamada 'Kayitli programlarim' bolumu).");
        return sb.toString();
    }

    private String buildPerformanceAnswer(AiAnalyzeResponseDTO analysis) {
        String top = analysis.weakTopics().isEmpty() ? "henuz tespit edilemedi"
                : analysis.weakTopics().get(0).dersAd() + " / " + analysis.weakTopics().get(0).konuAd();
        return "Son " + analysis.analyzedDays() + " gunde genel basari oranin %" + analysis.overallSuccessRate()
                + " (toplam " + analysis.totalAnswers() + " cevap).\n\n"
                + "En cok odaklanman gereken konu: " + top + ".\n\n"
                + "Net artirma icin: zayif konularda tekrar + zamanlama (bos birakma) calismasi onerilir.";
    }

    private String buildWelcomeAnswer(AiAnalyzeResponseDTO analysis) {
        return "Merhaba! Ben AI Ders Kocun. Senin cozecegin sorularin ve denemelerin uzerinden eksik konulari, "
                + "30 dakikalik hizli plani ve deneme sonrasi analizi cikarabilirim.\n\n"
                + "Kisaca durum: son " + analysis.analyzedDays() + " gunde basari %" + analysis.overallSuccessRate()
                + ".\n\nAsagidaki hazir sorulardan birine dokunabilir veya kendi mesajini yazabilirsin.";
    }

    private String buildDefaultGuidanceAnswer(AiAnalyzeResponseDTO analysis) {
        return "Mesajini biraz daha netlestirebilir misin? Ornegin: 'eksiklerim', '30 dakikada plan', 'deneme analizi'.\n\n"
                + "Kisaca: son donem basari %" + analysis.overallSuccessRate()
                + ", veri yeterliyse zayif konular listelenir."
                + (analysis.weakTopics().isEmpty() ? " Simdilik daha fazla soru cozmeni oneririm." : "");
    }

    public List<AiTrainingRowDTO> exportTrainingDataset(int days, int minAnswers) {
        int safeDays = Math.max(30, Math.min(days, 365));
        int safeMinAnswers = Math.max(5, Math.min(minAnswers, 500));
        Instant from = Instant.now().minus(safeDays, ChronoUnit.DAYS);
        List<Object[]> rows = cevapRepository.findTrainingRows(from, safeMinAnswers);

        List<AiTrainingRowDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            Long userId = toLong(r[0]);
            Long dersId = toLong(r[1]);
            String dersAd = String.valueOf(r[2]);
            Long konuId = toLong(r[3]);
            String konuAd = String.valueOf(r[4]);
            int total = toInt(r[5]);
            int correct = toInt(r[6]);
            int wrong = toInt(r[7]);
            int blank = toInt(r[8]);
            if (total <= 0) continue;

            double successRate = round2((correct * 100.0) / total);
            double wrongRate = round2((wrong * 100.0) / total);
            double blankRate = round2((blank * 100.0) / total);
            double volume = round2(Math.min(1.0, total / 50.0));
            // Etiket: proje icin baslangic "zayif konu" tanimi
            int riskHigh = (successRate < 60.0 || blankRate > 20.0) ? 1 : 0;

            out.add(new AiTrainingRowDTO(
                    userId, dersId, dersAd, konuId, konuAd,
                    total, correct, wrong, blank,
                    successRate, wrongRate, blankRate, volume, riskHigh
            ));
        }
        return out;
    }

    public AiAbCompareResponseDTO compareMlVsHeuristic(AppUser user, int days, int limit) {
        int safeDays = Math.max(7, Math.min(days, 180));
        int safeLimit = Math.max(1, Math.min(limit, 20));
        Instant from = Instant.now().minus(safeDays, ChronoUnit.DAYS);
        List<TopicStat> stats = toTopicStats(cevapRepository.findTopicPerformanceForUser(user.getId(), from));

        Map<String, MlPrediction> mlPredictions = fetchMlPredictions(stats);
        boolean mlResponded = !mlPredictions.isEmpty();
        String modelVersion = mlPredictions.values().stream()
                .map(MlPrediction::modelVersion)
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .orElse(null);

        List<AiAbTopicCompareDTO> comparisons = new ArrayList<>();
        for (TopicStat s : stats) {
            double heuristicRisk = calculateHeuristicRisk(s);
            String key = topicKey(s.dersId(), s.konuId());
            MlPrediction ml = mlPredictions.get(key);
            Double mlRisk = ml != null ? round2(ml.riskScore()) : null;
            Double delta = mlRisk != null ? round2(mlRisk - heuristicRisk) : null;
            String activeSource = ml != null && "ml".equalsIgnoreCase(ml.source()) ? "ml" : "heuristic";

            comparisons.add(new AiAbTopicCompareDTO(
                    s.dersId(),
                    s.dersAd(),
                    s.konuId(),
                    s.konuAd(),
                    heuristicRisk,
                    mlRisk,
                    delta,
                    activeSource,
                    ml != null ? ml.modelVersion() : null
            ));
        }

        comparisons = comparisons.stream()
                .sorted(Comparator.comparingDouble(AiAbTopicCompareDTO::heuristicRisk).reversed())
                .limit(safeLimit)
                .toList();

        return new AiAbCompareResponseDTO(safeDays, mlEnabled, mlResponded, modelVersion, comparisons);
    }

    private List<TopicStat> toTopicStats(List<Object[]> rows) {
        List<TopicStat> out = new ArrayList<>();
        for (Object[] r : rows) {
            Long dersId = toLong(r[0]);
            String dersAd = String.valueOf(r[1]);
            Long konuId = toLong(r[2]);
            String konuAd = String.valueOf(r[3]);
            int total = toInt(r[4]);
            int correct = toInt(r[5]);
            int wrong = toInt(r[6]);
            int blank = toInt(r[7]);
            if (total <= 0) continue;

            double successRate = round2((correct * 100.0) / total);
            out.add(new TopicStat(dersId, dersAd, konuId, konuAd, total, correct, wrong, blank, successRate));
        }
        return out;
    }

    private List<AiWeakTopicDTO> toWeakTopicList(List<TopicStat> stats) {
        Map<String, MlPrediction> mlPredictions = fetchMlPredictions(stats);
        List<AiWeakTopicDTO> out = new ArrayList<>();
        for (TopicStat s : stats) {
            double wrongRate = (s.wrongCount() * 100.0) / s.totalCount();
            double blankRate = (s.blankCount() * 100.0) / s.totalCount();

            double heuristicRisk = calculateHeuristicRisk(s);
            String heuristicRecommendation = recommendationFor(heuristicRisk, blankRate, s.successRate());

            String key = topicKey(s.dersId(), s.konuId());
            MlPrediction ml = mlPredictions.get(key);
            double risk = ml != null ? round2(ml.riskScore()) : heuristicRisk;
            String recommendation = (ml != null && ml.recommendation() != null && !ml.recommendation().isBlank())
                    ? ml.recommendation()
                    : heuristicRecommendation;

            out.add(new AiWeakTopicDTO(
                    s.dersId(), s.dersAd(), s.konuId(), s.konuAd(),
                    s.totalCount(), s.correctCount(), s.wrongCount(), s.blankCount(),
                    s.successRate(), risk, recommendation,
                    ml != null && ml.source() != null ? ml.source() : "heuristic",
                    ml != null ? ml.modelVersion() : null
            ));
        }
        return out;
    }

    private double calculateHeuristicRisk(TopicStat s) {
        double wrongRate = (s.wrongCount() * 100.0) / s.totalCount();
        double blankRate = (s.blankCount() * 100.0) / s.totalCount();
        double heuristicScore = (0.06 * wrongRate) + (0.08 * blankRate) + (s.totalCount() < 15 ? 0.6 : 0.0) - (0.05 * s.successRate());
        return round2(100.0 / (1.0 + Math.exp(-heuristicScore)));
    }

    private Map<String, MlPrediction> fetchMlPredictions(List<TopicStat> stats) {
        Map<String, MlPrediction> map = new HashMap<>();
        if (!mlEnabled || stats.isEmpty()) return map;

        try {
            MlPredictRequest payload = new MlPredictRequest(
                    stats.stream()
                            .map(s -> new MlTopicInput(
                                    topicKey(s.dersId(), s.konuId()),
                                    s.dersId(), s.konuId(),
                                    s.totalCount(), s.correctCount(), s.wrongCount(), s.blankCount(),
                                    s.successRate()
                            ))
                            .toList()
            );

            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mlBaseUrl + "/predict-weak-topics"))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(4))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) return map;

            MlPredictResponse parsed = objectMapper.readValue(response.body(), MlPredictResponse.class);
            if (parsed == null || parsed.predictions == null) return map;

            for (MlPrediction p : parsed.predictions) {
                if (p == null || p.key == null) continue;
                if (p.modelVersion == null || p.modelVersion.isBlank()) p.modelVersion = parsed.modelVersion;
                if (p.source == null || p.source.isBlank()) p.source = "ml";
                map.put(p.key, p);
            }
        } catch (Exception ignore) {
            // ML servisi yoksa ya da hata verirse sessizce fallback heuristic devam eder.
        }
        return map;
    }

    private String topicKey(Long dersId, Long konuId) {
        return String.valueOf(dersId) + ":" + String.valueOf(konuId);
    }

    private String recommendationFor(double risk, double blankRate, double successRate) {
        if (risk >= 75) {
            return "Yuksek risk: once konu anlatimi, sonra kisa testlerle ilerle.";
        }
        if (blankRate >= 20) {
            return "Bos birakma orani yuksek: soru hizini arttiracak deneme setleri cozulmeli.";
        }
        if (successRate < 60) {
            return "Basari dusuk: yanlis sorulara donup tekrar notu cikar.";
        }
        return "Takipte kal: gunde en az 15 hedef soru cozumu ile seviyeyi koru.";
    }

    private List<String> buildFocusTips(List<AiWeakTopicDTO> weak, double overallSuccessRate) {
        List<String> tips = new ArrayList<>();
        if (weak.isEmpty()) {
            tips.add("Yeterli veri yok. Daha dogru analiz icin en az 2-3 quiz cozmeye devam et.");
            return tips;
        }
        tips.add("Genel basari oranini %" + overallSuccessRate + " seviyesinden once %" + Math.min(85, overallSuccessRate + 8) + " hedefine cek.");
        tips.add("Ilk odak konusu: " + weak.get(0).dersAd() + " / " + weak.get(0).konuAd());
        tips.add("Her calisma sonunda en az 5 yanlis sorunun nedenini yazili tekrar et.");
        return tips;
    }

    private String buildPlanSummary(AiAnalyzeResponseDTO analysis, int dailyMinutes, String mode) {
        String topWeak = analysis.weakTopics().isEmpty() ? "genel tekrar" : analysis.weakTopics().get(0).konuAd();
        return "Gunluk " + dailyMinutes + " dakikalik " + mode
                + " hedefi ile yaklasik 7 gunluk gorev havuzu hazirlandi. Oncelik: " + topWeak
                + ". Plan, son " + analysis.analyzedDays() + " gun verine gore olusturuldu.";
    }

    private String normalizeMode(String mode) {
        String m = String.valueOf(mode == null ? "" : mode).toLowerCase(Locale.ROOT).trim();
        if ("video".equals(m) || "test".equals(m) || "mixed".equals(m)) return m;
        return "mixed";
    }

    private int toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private record TopicStat(
            Long dersId,
            String dersAd,
            Long konuId,
            String konuAd,
            int totalCount,
            int correctCount,
            int wrongCount,
            int blankCount,
            double successRate
    ) {}

    private record MlPredictRequest(List<MlTopicInput> topics) {}

    private record MlTopicInput(
            String key,
            Long dersId,
            Long konuId,
            int totalCount,
            int correctCount,
            int wrongCount,
            int blankCount,
            double successRate
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MlPredictResponse {
        public List<MlPrediction> predictions;
        public String modelVersion;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MlPrediction {
        public String key;
        public double riskScore;
        public String recommendation;
        public String source;
        public String modelVersion;

        public String key() { return key; }
        public double riskScore() { return riskScore; }
        public String recommendation() { return recommendation; }
        public String source() { return source; }
        public String modelVersion() { return modelVersion; }
    }
}
