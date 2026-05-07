package com.example.backend.service;

import com.example.backend.dto.GamificationDtos.*;
import com.example.backend.dto.RaporOzetDTO;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GamificationService {

    private static final DateTimeFormatter TR_DAY = DateTimeFormatter.ofPattern("d.M.yyyy");

    private final QuizService quizService;
    private final AppUserRepository userRepository;
    private final ObjectMapper objectMapper;

    public GamificationService(QuizService quizService, AppUserRepository userRepository, ObjectMapper objectMapper) {
        this.quizService = quizService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public GamificationStateDTO getState(AppUser user) {
        Map<String, Object> state = readState(user);
        ensureDailyTasks(user, state, false);
        saveState(user, state);
        return toDto(state);
    }

    @Transactional
    public GamificationSyncResponseDTO sync(AppUser user) {
        Map<String, Object> state = readState(user);
        Map<String, Integer> totals = calculateTotals(user);

        Map<String, Integer> last = toIntMap(state.get("lastReportTotals"));
        int deltaSolved = Math.max(0, totals.get("solved") - last.getOrDefault("solved", 0));
        int deltaCorrect = Math.max(0, totals.get("correct") - last.getOrDefault("correct", 0));
        int deltaSessions = Math.max(0, totals.get("sessions") - last.getOrDefault("sessions", 0));

        List<Map<String, Object>> activeEffects = cleanupActiveEffects(state);
        boolean xpBoost = activeEffects.stream().anyMatch(x -> "xp_boost_24h".equals(String.valueOf(x.get("id"))));
        boolean goldBoost = activeEffects.stream().anyMatch(x -> "gold_boost_24h".equals(String.valueOf(x.get("id"))));

        int rawPoints = deltaSolved + (deltaCorrect * 5) + (deltaSessions * 2);
        int earnedPoints = xpBoost ? Math.round(rawPoints * 1.2f) : rawPoints;
        int earnedGold = Math.max(0, earnedPoints / 20);
        if (goldBoost) {
            earnedGold = Math.round(earnedGold * 1.3f);
        }

        int points = toInt(state.get("points")) + earnedPoints;
        int gold = toInt(state.get("gold")) + earnedGold;

        state.put("points", Math.max(0, points));
        state.put("gold", Math.max(0, gold));
        state.put("activeEffects", activeEffects);
        state.put("lastReportTotals", totals);
        ensureDailyTasks(user, state, false);
        autoCompleteTasks(state, totals);

        saveState(user, state);
        return new GamificationSyncResponseDTO(toDto(state), earnedPoints, earnedGold);
    }

    @Transactional
    public GamificationStateDTO completeTask(AppUser user, String taskId) {
        if (taskId == null || taskId.isBlank()) throw new IllegalArgumentException("taskId zorunlu");
        Map<String, Object> state = readState(user);
        ensureDailyTasks(user, state, false);

        List<Map<String, Object>> tasks = toMapList(state.get("dailyTasks"));
        Map<String, Integer> totals = calculateTotals(user);
        boolean completedNow = false;

        for (Map<String, Object> task : tasks) {
            if (!taskId.equals(String.valueOf(task.get("id")))) continue;
            if (Boolean.TRUE.equals(task.get("completed"))) break;
            String metric = String.valueOf(task.get("metric"));
            int target = toInt(task.get("target"));
            int value = totals.getOrDefault(metric, 0);
            if (value < target) {
                throw new IllegalArgumentException("Gorev henuz tamamlanmadi");
            }
            task.put("completed", true);
            state.put("points", toInt(state.get("points")) + toInt(task.get("rewardPoints")));
            state.put("gold", toInt(state.get("gold")) + toInt(task.get("rewardGold")));
            completedNow = true;
            break;
        }
        if (!completedNow) {
            throw new IllegalArgumentException("Gorev bulunamadi veya zaten tamamlanmis");
        }
        state.put("dailyTasks", tasks);
        saveState(user, state);
        return toDto(state);
    }

    @Transactional
    public GamificationStateDTO purchase(AppUser user, String itemId) {
        if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId zorunlu");
        MarketItem item = MarketItem.byId(itemId);
        if (item == null) throw new IllegalArgumentException("Gecersiz market urunu");

        Map<String, Object> state = readState(user);
        int gold = toInt(state.get("gold"));
        if (gold < item.price) throw new IllegalArgumentException("Yetersiz altin");

        List<String> owned = toStringList(state.get("ownedItems"));
        boolean repeatable = item.id.startsWith("instant_");
        if (!repeatable && owned.contains(item.id)) {
            throw new IllegalArgumentException("Bu urun zaten satin alinmis");
        }

        state.put("gold", gold - item.price);
        if (!repeatable) {
            owned.add(item.id);
            state.put("ownedItems", owned);
        }

        switch (item.id) {
            case "xp_boost_24h", "gold_boost_24h" -> {
                List<Map<String, Object>> effects = cleanupActiveEffects(state);
                effects.removeIf(x -> item.id.equals(String.valueOf(x.get("id"))));
                effects.add(Map.of(
                        "id", item.id,
                        "expiresAt", Instant.now().plusSeconds(24 * 60 * 60).toString()
                ));
                state.put("activeEffects", effects);
            }
            case "instant_points_pack" -> state.put("points", toInt(state.get("points")) + 120);
            case "instant_gold_pack" -> state.put("gold", toInt(state.get("gold")) + 40);
            case "extra_task_slot" -> {
                // Yeni gunde gorev olusurken ek slotu acik tutar.
            }
            default -> throw new IllegalArgumentException("Desteklenmeyen urun");
        }

        saveState(user, state);
        return toDto(state);
    }

    public List<MarketItemDTO> marketItems() {
        return MarketItem.ALL.stream()
                .map(i -> new MarketItemDTO(i.id, i.label, i.desc, i.price))
                .toList();
    }

    private void autoCompleteTasks(Map<String, Object> state, Map<String, Integer> totals) {
        List<Map<String, Object>> tasks = toMapList(state.get("dailyTasks"));
        int extraPoints = 0;
        int extraGold = 0;
        for (Map<String, Object> task : tasks) {
            if (Boolean.TRUE.equals(task.get("completed"))) continue;
            String metric = String.valueOf(task.get("metric"));
            int target = toInt(task.get("target"));
            int value = totals.getOrDefault(metric, 0);
            if (value >= target) {
                task.put("completed", true);
                extraPoints += toInt(task.get("rewardPoints"));
                extraGold += toInt(task.get("rewardGold"));
            }
        }
        state.put("dailyTasks", tasks);
        if (extraPoints > 0 || extraGold > 0) {
            state.put("points", toInt(state.get("points")) + extraPoints);
            state.put("gold", toInt(state.get("gold")) + extraGold);
        }
    }

    private void ensureDailyTasks(AppUser user, Map<String, Object> state, boolean forceRegenerate) {
        String today = LocalDate.now().format(TR_DAY);
        String stateDate = String.valueOf(state.getOrDefault("dailyTaskDate", ""));
        if (!forceRegenerate && today.equals(stateDate)) return;

        Map<String, Integer> totals = calculateTotals(user);
        int solvedTarget = Math.max(15, Math.min(60, Math.round(totals.get("solved") * 0.5f) + 15));
        int correctTarget = Math.max(8, Math.min(40, Math.round(totals.get("correct") * 0.5f) + 8));
        int sessionTarget = Math.max(1, Math.min(4, totals.get("sessions") > 0 ? 2 : 1));

        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(task("ai_" + System.currentTimeMillis() + "_s", "Bugun " + solvedTarget + " soru coz", "solved", solvedTarget, 30, 4));
        tasks.add(task("ai_" + System.currentTimeMillis() + "_c", correctTarget + " dogruya ulas", "correct", correctTarget, 24, 3));
        tasks.add(task("ai_" + System.currentTimeMillis() + "_x", sessionTarget + " oturum tamamla", "sessions", sessionTarget, 16, 2));

        List<String> owned = toStringList(state.get("ownedItems"));
        if (owned.contains("extra_task_slot")) {
            int extraTarget = Math.max(20, solvedTarget + 10);
            tasks.add(task("ai_" + System.currentTimeMillis() + "_e", "Ek gorev: " + extraTarget + " soru", "solved", extraTarget, 40, 5));
        }

        state.put("dailyTaskDate", today);
        state.put("dailyTasks", tasks);
    }

    private Map<String, Object> task(String id, String title, String metric, int target, int rewardPoints, int rewardGold) {
        Map<String, Object> out = new HashMap<>();
        out.put("id", id);
        out.put("title", title);
        out.put("metric", metric);
        out.put("target", target);
        out.put("rewardPoints", rewardPoints);
        out.put("rewardGold", rewardGold);
        out.put("completed", false);
        out.put("assignedBy", "ai-rule-engine");
        return out;
    }

    private Map<String, Integer> calculateTotals(AppUser user) {
        List<RaporOzetDTO> raporlar = quizService.listOzetForUser(user, 500);
        int solved = raporlar.stream().mapToInt(r -> r.totalCount() != null ? r.totalCount() : 0).sum();
        int correct = raporlar.stream().mapToInt(r -> r.correctCount() != null ? r.correctCount() : 0).sum();
        int sessions = raporlar.size();
        return Map.of("solved", solved, "correct", correct, "sessions", sessions);
    }

    private List<Map<String, Object>> cleanupActiveEffects(Map<String, Object> state) {
        List<Map<String, Object>> effects = toMapList(state.get("activeEffects"));
        Instant now = Instant.now();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> effect : effects) {
            String expiresAt = String.valueOf(effect.get("expiresAt"));
            if (expiresAt == null || expiresAt.isBlank() || "null".equals(expiresAt)) {
                out.add(effect);
                continue;
            }
            try {
                Instant expires = Instant.parse(expiresAt);
                if (expires.isAfter(now)) out.add(effect);
            } catch (Exception ignored) {
                // Bozuk veri varsa effect'i dusur
            }
        }
        return out;
    }

    private GamificationStateDTO toDto(Map<String, Object> state) {
        List<DailyTaskDTO> tasks = toMapList(state.get("dailyTasks")).stream()
                .map(t -> new DailyTaskDTO(
                        String.valueOf(t.get("id")),
                        String.valueOf(t.get("title")),
                        String.valueOf(t.get("metric")),
                        toInt(t.get("target")),
                        toInt(t.get("rewardPoints")),
                        toInt(t.get("rewardGold")),
                        Boolean.TRUE.equals(t.get("completed")),
                        String.valueOf(t.getOrDefault("assignedBy", "ai-rule-engine"))
                )).toList();

        List<ActiveEffectDTO> effects = toMapList(state.get("activeEffects")).stream()
                .map(e -> new ActiveEffectDTO(
                        String.valueOf(e.get("id")),
                        e.get("expiresAt") != null ? String.valueOf(e.get("expiresAt")) : null
                )).toList();

        Map<String, Integer> totals = toIntMap(state.get("lastReportTotals"));
        return new GamificationStateDTO(
                toInt(state.get("points")),
                toInt(state.get("gold")),
                String.valueOf(state.getOrDefault("dailyTaskDate", "")),
                tasks,
                toStringList(state.get("ownedItems")),
                effects,
                totals.getOrDefault("solved", 0),
                totals.getOrDefault("correct", 0),
                totals.getOrDefault("sessions", 0)
        );
    }

    private Map<String, Object> readState(AppUser user) {
        Map<String, Object> state = new LinkedHashMap<>();
        if (user.getGamificationState() != null && !user.getGamificationState().isBlank()) {
            try {
                state.putAll(objectMapper.readValue(user.getGamificationState(), new TypeReference<Map<String, Object>>() {}));
            } catch (Exception ignored) {
                // Bozuk JSON varsa sifirdan olusturulur.
            }
        }
        state.putIfAbsent("points", user.getPuan() != null ? user.getPuan() : 0);
        state.putIfAbsent("gold", user.getAltin() != null ? user.getAltin() : 0);
        state.putIfAbsent("ownedItems", new ArrayList<>());
        state.putIfAbsent("activeEffects", new ArrayList<>());
        state.putIfAbsent("dailyTasks", new ArrayList<>());
        state.putIfAbsent("dailyTaskDate", "");
        state.putIfAbsent("lastReportTotals", Map.of("solved", 0, "correct", 0, "sessions", 0));
        return state;
    }

    private void saveState(AppUser user, Map<String, Object> state) {
        user.setPuan(Math.max(0, toInt(state.get("points"))));
        user.setAltin(Math.max(0, toInt(state.get("gold"))));
        try {
            user.setGamificationState(objectMapper.writeValueAsString(state));
        } catch (Exception e) {
            throw new IllegalStateException("Gamification state serilestirilemedi", e);
        }
        userRepository.save(user);
    }

    private int toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return 0;
        }
    }

    private List<String> toStringList(Object value) {
        if (!(value instanceof List<?> list)) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        for (Object o : list) out.add(String.valueOf(o));
        return out;
    }

    private List<Map<String, Object>> toMapList(Object value) {
        if (!(value instanceof List<?> list)) return new ArrayList<>();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> m) {
                Map<String, Object> converted = new LinkedHashMap<>();
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    converted.put(String.valueOf(e.getKey()), e.getValue());
                }
                out.add(converted);
            }
        }
        return out;
    }

    private Map<String, Integer> toIntMap(Object value) {
        if (!(value instanceof Map<?, ?> m)) return new HashMap<>();
        Map<String, Integer> out = new HashMap<>();
        for (Map.Entry<?, ?> e : m.entrySet()) {
            out.put(String.valueOf(e.getKey()), toInt(e.getValue()));
        }
        return out;
    }

    private record MarketItem(String id, String label, String desc, int price) {
        static final List<MarketItem> ALL = List.of(
                new MarketItem("xp_boost_24h", "24 Saat XP Boost", "24 saat boyunca puan kazancin %20 artar", 60),
                new MarketItem("gold_boost_24h", "24 Saat Altin Boost", "24 saat boyunca altin kazancin %30 artar", 45),
                new MarketItem("extra_task_slot", "Ek Gunluk Gorev Slotu", "Her gun 1 ekstra AI gorevi acilir", 110),
                new MarketItem("instant_points_pack", "Anlik +120 Puan", "Hemen 120 puan verir", 30),
                new MarketItem("instant_gold_pack", "Anlik +40 Altin", "Hemen 40 altin verir", 50)
        );

        static MarketItem byId(String id) {
            return ALL.stream().filter(i -> i.id.equals(id)).findFirst().orElse(null);
        }
    }
}
