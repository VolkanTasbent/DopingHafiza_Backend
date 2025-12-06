package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.*;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizService {

    private final DersRepository dersRepo;
    private final SoruRepository soruRepo;
    private final SecenekRepository secenekRepo;
    private final QuizOturumuRepository oturumRepo;
    private final CevapRepository cevapRepo;
    private final DenemeSinaviRepository denemeRepo;
    private final DenemeSinaviSoruRepository denemeSoruRepo;
    private final DenemeSinaviCevapRepository denemeCevapRepo;
    private final com.example.backend.repository.UserActivityRepository userActivityRepo;

    public QuizService(
            DersRepository dersRepo,
            SoruRepository soruRepo,
            SecenekRepository secenekRepo,
            QuizOturumuRepository oturumRepo,
            CevapRepository cevapRepo,
            DenemeSinaviRepository denemeRepo,
            DenemeSinaviSoruRepository denemeSoruRepo,
            DenemeSinaviCevapRepository denemeCevapRepo,
            com.example.backend.repository.UserActivityRepository userActivityRepo
    ) {
        this.dersRepo = dersRepo;
        this.soruRepo = soruRepo;
        this.secenekRepo = secenekRepo;
        this.oturumRepo = oturumRepo;
        this.cevapRepo = cevapRepo;
        this.denemeRepo = denemeRepo;
        this.denemeSoruRepo = denemeSoruRepo;
        this.denemeCevapRepo = denemeCevapRepo;
        this.userActivityRepo = userActivityRepo;
    }

    /** ✅ Quiz sonuçlarını kaydeder */
    @Transactional
    public SubmitResponseDTO submit(QuizSubmitRequest req, AppUser user) {
        Instant started  = Optional.ofNullable(req.startedAt()).orElse(Instant.now());
        Instant finished = Optional.ofNullable(req.finishedAt()).orElse(Instant.now());
        long durationMs  = Duration.between(started, finished).toMillis();

        QuizOturumu oturum = new QuizOturumu();
        oturum.setUser(user);
        oturum.setStartedAt(started);
        oturum.setFinishedAt(finished);
        oturum.setDurationMs(durationMs);
        oturum = oturumRepo.save(oturum);

        int total = 0, correct = 0, wrong = 0, empty = 0;

        if (req.items() != null) {
            for (QuizSubmitItemDTO it : req.items()) {
                total++;

                Soru soru = soruRepo.findById(it.soruId())
                        .orElseThrow(() -> new IllegalArgumentException("Soru yok: " + it.soruId()));

                Secenek secenek = null;
                boolean dogru = false;
                boolean bosmu = (it.secenekId() == null);

                if (!bosmu) {
                    secenek = secenekRepo.findById(it.secenekId()).orElse(null);
                    if (secenek != null) {
                        dogru = secenek.isDogru();
                    }
                }

                // 🧩 Debug log
                System.out.println("🧠 SoruID=" + it.soruId() +
                        " | SeçenekID=" + it.secenekId() +
                        " | Dogru=" + dogru +
                        " | Boş=" + bosmu);

                // Puanlama: Doğru +3, Yanlış -1, Boş 0
                if (bosmu) {
                    empty++;
                } else if (dogru) {
                    correct++;
                } else {
                    wrong++;
                }

                Cevap c = new Cevap();
                c.setOturum(oturum);
                c.setSoru(soru);
                c.setSecenek(secenek);
                c.setDogru(dogru);
                cevapRepo.save(c);
            }
        }

        // YKS Net Hesaplama: Net = Doğru - (Yanlış / 4)
        // Her doğru +1 net, her yanlış -0.25 net
        double net = correct - (wrong / 4.0);

        oturum.setTotal(total);
        oturum.setCorrect(correct);
        oturum.setWrong(wrong);
        oturum.setEmpty(empty);
        oturum.setScore((int) Math.round(net));  // Yuvarlanmış net değeri (score legacy için)
        oturumRepo.save(oturum);

        System.out.println("✅ Oturum " + oturum.getId() + " kaydedildi. Doğru=" + correct + " | Yanlış=" + wrong + " | Boş=" + empty + " | Net=" + String.format("%.2f", net));

        // Aktivite kaydet
        if (user != null && total > 0) {
            try {
                // İlk sorudan ders ve konu bilgilerini al
                Soru ilkSoru = null;
                Ders ders = null;
                Konu konu = null;
                
                if (req.items() != null && !req.items().isEmpty()) {
                    ilkSoru = soruRepo.findById(req.items().get(0).soruId()).orElse(null);
                    if (ilkSoru != null) {
                        ders = ilkSoru.getDers();
                        // İlk konuyu al (eğer varsa)
                        if (!ilkSoru.getKonular().isEmpty()) {
                            konu = ilkSoru.getKonular().iterator().next();
                        }
                    }
                }

                com.example.backend.model.UserActivity activity = new com.example.backend.model.UserActivity();
                activity.setUserId(user.getId());
                activity.setActivityType("soru_cozme");
                
                // Başlık oluştur
                String title = "Soru Çözme";
                if (ders != null && ders.getAd() != null) {
                    title = ders.getAd();
                    if (konu != null && konu.getAd() != null) {
                        title += " > " + konu.getAd();
                    }
                }
                activity.setActivityTitle(title);
                
                // Alt başlık: Konu detayı veya genel bilgi
                String subtitle = total + " soru çözüldü";
                if (konu != null && konu.getAd() != null) {
                    subtitle = konu.getAd();
                }
                activity.setActivitySubtitle(subtitle);
                
                activity.setActivityIcon("abc");
                activity.setDersId(ders != null ? ders.getId() : null);
                activity.setKonuId(konu != null ? konu.getId() : null);
                activity.setRaporId(oturum.getId());
                
                // Metadata
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("soruSayisi", total);
                metadata.put("dogru", correct);
                metadata.put("yanlis", wrong);
                metadata.put("net", net);
                activity.setMetadata(metadata);
                
                userActivityRepo.save(activity);
                System.out.println("✅ Aktivite kaydedildi: " + title);
            } catch (Exception e) {
                System.err.println("⚠️ Aktivite kaydedilirken hata: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return new SubmitResponseDTO(oturum.getId(), correct, wrong, empty, total, net);
    }

    /** ✅ Kullanıcının rapor özetlerini listeler */
    @Transactional(readOnly = true)
    public List<RaporOzetDTO> listOzetForUser(AppUser user, Integer limit) {
        var page = oturumRepo.findByUser(
                user,
                org.springframework.data.domain.PageRequest.of(0, limit != null ? limit : 20)
        );
        return page.getContent().stream()
                .map(o -> {
                    // Net hesapla: Doğru - (Yanlış / 4)
                    double net = (o.getCorrect() != null ? o.getCorrect() : 0)
                            - ((o.getWrong() != null ? o.getWrong() : 0) / 4.0);
                    return new RaporOzetDTO(
                            o.getId(),
                            o.getFinishedAt(), // Instant olarak doğrudan
                            o.getTotal(),      // totalCount olarak
                            o.getCorrect(),
                            o.getWrong(),
                            o.getEmpty(),
                            o.getDurationMs(),
                            net
                    );
                })
                .toList();
    }

    /** ✅ Rapor detaylarını getirir (LazyInitialization fix + DTO dönüşümü) - DERS BİLGİSİ EKLENDİ */
    @Transactional(readOnly = true)
    public RaporDetayDTO detayForUser(AppUser user, Long oturumId, boolean isAdmin) {
        QuizOturumu o = isAdmin
                ? oturumRepo.findById(oturumId)
                .orElseThrow(() -> new IllegalArgumentException("Oturum yok: " + oturumId))
                : oturumRepo.findByIdAndUser(oturumId, user)
                .orElseThrow(() -> new IllegalArgumentException("Oturum bulunamadı veya yetkisiz"));

        // Deneme sınavı kontrolü
        if (o.getDenemeSinavi() != null) {
            // Deneme sınavı için soruları getir
            return getDenemeSinaviDetay(o);
        }

        // Normal quiz için - DERS BİLGİSİ İLE BİRLİKTE
        var list = cevapRepo.findByOturum(o);

        // Debug: Ders bilgisi kontrolü
        System.out.println("🔍 Rapor detayı için " + list.size() + " cevap bulundu");
        if (!list.isEmpty()) {
            Soru ilkSoru = list.get(0).getSoru();
            String dersAdi = ilkSoru.getDers() != null ?
                    (ilkSoru.getDers().getAd() != null ? ilkSoru.getDers().getAd() : "Ders Adı Yok") :
                    "Ders Entity Yok";
            System.out.println("📚 İlk soru ders bilgisi: " + dersAdi);
        }

        var items = list.stream().map(c -> {
            Soru s = c.getSoru(); // Lazy fix - transactional açık
            var sDto = mapToSoruDTO(s); // Bu metod artık ders bilgisini içeriyor
            return new RaporDetayItemDTO(
                    c.getId(),
                    sDto,
                    c.getSecenek() != null ? c.getSecenek().getId() : null,
                    c.isDogru()
            );
        }).toList();

        return new RaporDetayDTO(o.getId(), items);
    }

    /** Deneme sınavı detaylarını getirir */
    @Transactional(readOnly = true)
    protected RaporDetayDTO getDenemeSinaviDetay(QuizOturumu oturum) {
        DenemeSinavi deneme = oturum.getDenemeSinavi();
        List<DenemeSinaviSoru> sorular = denemeSoruRepo.findByDenemeSinaviOrderBySoruNoAsc(deneme);
        List<DenemeSinaviCevap> cevaplar = denemeCevapRepo.findByOturumOrderBySoruNoAsc(oturum);

        // Cevap map'i oluştur (soruNo -> cevap)
        Map<Integer, DenemeSinaviCevap> cevapMap = cevaplar.stream()
                .collect(java.util.stream.Collectors.toMap(DenemeSinaviCevap::getSoruNo, c -> c));

        var items = sorular.stream().map(soru -> {
            // Deneme sınavı sorularını SoruDTO formatına çevir
            var sDto = convertDenemeSoruToSoruDTO(soru);

            // Cevap bilgisini bul
            DenemeSinaviCevap cevap = cevapMap.get(soru.getSoruNo());
            Boolean dogruMu = cevap != null ? cevap.isDogru() : null;
            Long secenekId = null;

            // Seçilen cevabı fake ID'ye çevir
            if (cevap != null && cevap.getSecilenCevap() != null) {
                String secilen = cevap.getSecilenCevap().trim().toUpperCase();
                // Fake ID formatı: soruId * 1000 + sıralama (A=1, B=2, C=3, D=4, E=5)
                int siralama = secilen.charAt(0) - 'A' + 1;
                if (siralama >= 1 && siralama <= 5) {
                    secenekId = soru.getId() * 1000L + siralama;
                }
            }

            // Unique ID oluştur: cevap varsa cevap ID, yoksa soru ID + soruNo kombinasyonu
            // Bu sayede her item için benzersiz bir ID garantilenir
            Long uniqueId;
            if (cevap != null) {
                uniqueId = cevap.getId();
            } else {
                // Soru ID + soruNo kombinasyonu ile unique ID oluştur
                // Örnek: Soru ID=109, SoruNo=5 -> 1090005
                uniqueId = soru.getId() * 10000L + soru.getSoruNo();
            }

            return new RaporDetayItemDTO(
                    uniqueId,
                    sDto,
                    secenekId,
                    dogruMu
            );
        }).toList();

        return new RaporDetayDTO(oturum.getId(), items);
    }

    /** Deneme sınavı sorusunu SoruDTO'ya çevirir */
    private SoruDTO convertDenemeSoruToSoruDTO(DenemeSinaviSoru soru) {
        // Şıkları SecenekDTO listesine çevir
        List<SecenekDTO> secenekler = new ArrayList<>();
        int siralama = 1;
        if (soru.getSikA() != null && !soru.getSikA().trim().isEmpty()) {
            Long fakeId = soru.getId() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, soru.getSikA().trim(),
                    soru.getDogruCevap() != null && soru.getDogruCevap().equalsIgnoreCase("A"), siralama++));
        }
        if (soru.getSikB() != null && !soru.getSikB().trim().isEmpty()) {
            Long fakeId = soru.getId() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, soru.getSikB().trim(),
                    soru.getDogruCevap() != null && soru.getDogruCevap().equalsIgnoreCase("B"), siralama++));
        }
        if (soru.getSikC() != null && !soru.getSikC().trim().isEmpty()) {
            Long fakeId = soru.getId() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, soru.getSikC().trim(),
                    soru.getDogruCevap() != null && soru.getDogruCevap().equalsIgnoreCase("C"), siralama++));
        }
        if (soru.getSikD() != null && !soru.getSikD().trim().isEmpty()) {
            Long fakeId = soru.getId() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, soru.getSikD().trim(),
                    soru.getDogruCevap() != null && soru.getDogruCevap().equalsIgnoreCase("D"), siralama++));
        }
        if (soru.getSikE() != null && !soru.getSikE().trim().isEmpty()) {
            Long fakeId = soru.getId() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, soru.getSikE().trim(),
                    soru.getDogruCevap() != null && soru.getDogruCevap().equalsIgnoreCase("E"), siralama++));
        }

        // Konuları parse et
        List<KonuDTO> konular = new ArrayList<>();
        if (soru.getKonular() != null && !soru.getKonular().trim().isEmpty()) {
            String[] konuAdlari = soru.getKonular().split(",");
            for (String konuAdi : konuAdlari) {
                String trimmed = konuAdi.trim();
                if (!trimmed.isEmpty()) {
                    konular.add(new KonuDTO(null, trimmed, "", "", ""));
                }
            }
        }

        return new SoruDTO(
                soru.getId(),
                soru.getSoruMetni() != null ? soru.getSoruMetni() : "",
                "coktan_secmeli",
                soru.getZorluk(),
                "",
                soru.getDers() != null && soru.getDers().getAd() != null ? soru.getDers().getAd() : "Genel",
                konular,
                secenekler,
                soru.getCozumVideosuUrl() != null ? soru.getCozumVideosuUrl() : ""
        );
    }

    /** ✅ Soru -> DTO dönüşümü - DERS BİLGİSİ EKLENDİ */
    private SoruDTO mapToSoruDTO(Soru s) {
        // Ders bilgisini kontrol et ve işle
        String dersAdi = "Genel"; // Varsayılan değer

        if (s.getDers() != null) {
            // Ders entity'sini kontrol et
            Ders ders = s.getDers();
            if (ders.getAd() != null && !ders.getAd().trim().isEmpty()) {
                dersAdi = ders.getAd().trim();
            } else {
                System.out.println("⚠️ UYARI: Soru " + s.getId() + " için ders adı boş!");
                dersAdi = "Ders Adı Yok";
            }
        } else {
            System.out.println("⚠️ UYARI: Soru " + s.getId() + " için ders bilgisi yok!");
            dersAdi = "Genel";
        }

        var konular = s.getKonular().stream()
                .map(k -> new KonuDTO(
                        k.getId(),
                        k.getAd() != null ? k.getAd() : "",
                        k.getDokumanUrl() != null ? k.getDokumanUrl() : "",
                        k.getDokumanAdi() != null ? k.getDokumanAdi() : "",
                        k.getKonuAnlatimVideosuUrl() != null ? k.getKonuAnlatimVideosuUrl() : ""))
                .toList();

        var secenekler = secenekRepo.findBySoruOrderBySiralamaAscIdAsc(s).stream()
                .map(o -> new SecenekDTO(o.getId(), o.getMetin() != null ? o.getMetin() : "", o.isDogru(), o.getSiralama()))
                .toList();

        return new SoruDTO(
                s.getId(),
                s.getMetin() != null ? s.getMetin() : "",
                s.getTip() != null ? s.getTip() : "",
                s.getZorluk(),
                s.getImageUrl() != null ? s.getImageUrl() : "",
                dersAdi, // DÜZELTİLDİ: Ders bilgisi eklendi
                konular,
                secenekler,
                s.getCozumVideosuUrl() != null ? s.getCozumVideosuUrl() : ""
        );
    }

    /** Deneme sınavı çözme - Submit */
    @Transactional
    public SubmitResponseDTO submitDenemeSinavi(DenemeSinaviSubmitRequest req, AppUser user) {
        DenemeSinavi deneme = denemeRepo.findById(req.denemeSinaviId())
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + req.denemeSinaviId()));

        Instant started = Optional.ofNullable(req.startedAt()).orElse(Instant.now());
        Instant finished = Optional.ofNullable(req.finishedAt()).orElse(Instant.now());
        long durationMs = java.time.Duration.between(started, finished).toMillis();

        QuizOturumu oturum = new QuizOturumu();
        oturum.setUser(user);
        oturum.setDenemeSinavi(deneme);
        oturum.setStartedAt(started);
        oturum.setFinishedAt(finished);
        oturum.setDurationMs(durationMs);
        oturum = oturumRepo.save(oturum);

        // Tüm soruları getir
        List<DenemeSinaviSoru> sorular = denemeSoruRepo.findByDenemeSinaviOrderBySoruNoAsc(deneme);
        Map<Integer, DenemeSinaviSoru> soruMap = sorular.stream()
                .collect(java.util.stream.Collectors.toMap(DenemeSinaviSoru::getSoruNo, s -> s));

        int total = sorular.size();
        int correct = 0, wrong = 0, empty = 0;

        // Cevap map'i oluştur
        Map<Integer, String> cevaplar = new HashMap<>();
        if (req.items() != null) {
            System.out.println("🔍 Cevap map'i oluşturuluyor, items sayısı: " + req.items().size());
            for (DenemeSinaviSubmitItemDTO item : req.items()) {
                System.out.println("  ➕ SoruNo: " + item.soruNo() + " -> Cevap: '" + item.secilenCevap() + "'");
                cevaplar.put(item.soruNo(), item.secilenCevap());
            }
            System.out.println("✅ Cevap map'i oluşturuldu, toplam: " + cevaplar.size() + " cevap");
        } else {
            System.out.println("⚠️ req.items() null!");
        }

        // Her soru için kontrol et ve cevapları kaydet
        System.out.println("📊 " + sorular.size() + " soru kontrol ediliyor...");
        System.out.println("🔍 Cevap map içeriği: " + cevaplar);
        for (DenemeSinaviSoru soru : sorular) {
            String secilen = cevaplar.get(soru.getSoruNo());
            boolean bosmu = (secilen == null || secilen.trim().isEmpty());

            boolean dogru = false;
            if (!bosmu) {
                String dogruCevap = soru.getDogruCevap();
                dogru = dogruCevap != null && dogruCevap.trim().equalsIgnoreCase(secilen.trim());
                System.out.println("  ❓ Soru " + soru.getSoruNo() + ": Seçilen='" + secilen + "', Doğru='" + dogruCevap + "', Sonuç=" + (dogru ? "✅" : "❌"));
            } else {
                System.out.println("  ⚪ Soru " + soru.getSoruNo() + ": Boş (seçilen: " + secilen + ", map'te var mı: " + cevaplar.containsKey(soru.getSoruNo()) + ")");
            }

            // Cevabı kaydet
            DenemeSinaviCevap cevap = new DenemeSinaviCevap();
            cevap.setOturum(oturum);
            cevap.setDenemeSinaviSoru(soru);
            cevap.setSoruNo(soru.getSoruNo());
            cevap.setSecilenCevap(bosmu ? null : secilen.trim().toUpperCase());
            cevap.setDogru(dogru);
            denemeCevapRepo.save(cevap);

            if (bosmu) {
                empty++;
            } else if (dogru) {
                correct++;
            } else {
                wrong++;
            }
        }
        System.out.println("📈 Sonuç: Doğru=" + correct + ", Yanlış=" + wrong + ", Boş=" + empty);

        // YKS Net Hesaplama
        double net = correct - (wrong / 4.0);

        oturum.setTotal(total);
        oturum.setCorrect(correct);
        oturum.setWrong(wrong);
        oturum.setEmpty(empty);
        oturum.setScore((int) Math.round(net));
        oturumRepo.save(oturum);

        // Aktivite kaydet
        if (user != null && total > 0) {
            try {
                com.example.backend.model.UserActivity activity = new com.example.backend.model.UserActivity();
                activity.setUserId(user.getId());
                activity.setActivityType("soru_cozme");
                activity.setActivityTitle(deneme.getAd() != null ? deneme.getAd() : "Deneme Sınavı");
                activity.setActivitySubtitle(deneme.getTip() != null ? deneme.getTip() + " - " + total + " soru" : total + " soru çözüldü");
                activity.setActivityIcon("abc");
                activity.setRaporId(oturum.getId());
                
                // Metadata
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("soruSayisi", total);
                metadata.put("dogru", correct);
                metadata.put("yanlis", wrong);
                metadata.put("net", net);
                metadata.put("denemeSinaviId", deneme.getId());
                activity.setMetadata(metadata);
                
                userActivityRepo.save(activity);
                System.out.println("✅ Deneme sınavı aktivitesi kaydedildi: " + deneme.getAd());
            } catch (Exception e) {
                System.err.println("⚠️ Deneme sınavı aktivitesi kaydedilirken hata: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return new SubmitResponseDTO(oturum.getId(), correct, wrong, empty, total, net);
    }
}