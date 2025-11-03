package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.*;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
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

    public QuizService(
            DersRepository dersRepo,
            SoruRepository soruRepo,
            SecenekRepository secenekRepo,
            QuizOturumuRepository oturumRepo,
            CevapRepository cevapRepo,
            DenemeSinaviRepository denemeRepo,
            DenemeSinaviSoruRepository denemeSoruRepo
    ) {
        this.dersRepo = dersRepo;
        this.soruRepo = soruRepo;
        this.secenekRepo = secenekRepo;
        this.oturumRepo = oturumRepo;
        this.cevapRepo = cevapRepo;
        this.denemeRepo = denemeRepo;
        this.denemeSoruRepo = denemeSoruRepo;
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
                            o.getFinishedAt(),
                            o.getTotal(),
                            o.getCorrect(),
                            o.getWrong(),
                            o.getEmpty(),
                            o.getDurationMs(),
                            net
                    );
                })
                .toList();
    }

    /** ✅ Rapor detaylarını getirir (LazyInitialization fix + DTO dönüşümü) */
    @Transactional(readOnly = true)
    public RaporDetayDTO detayForUser(AppUser user, Long oturumId, boolean isAdmin) {
        QuizOturumu o = isAdmin
                ? oturumRepo.findById(oturumId)
                .orElseThrow(() -> new IllegalArgumentException("Oturum yok: " + oturumId))
                : oturumRepo.findByIdAndUser(oturumId, user)
                .orElseThrow(() -> new IllegalArgumentException("Oturum bulunamadı veya yetkisiz"));

        var list = cevapRepo.findByOturum(o);

        var items = list.stream().map(c -> {
            Soru s = c.getSoru(); // Lazy fix - transactional açık
            var sDto = mapToSoruDTO(s);
            return new RaporDetayItemDTO(
                    c.getId(),
                    sDto,
                    c.getSecenek() != null ? c.getSecenek().getId() : null,
                    c.isDogru()
            );
        }).toList();

        return new RaporDetayDTO(o.getId(), items);
    }

    /** ✅ Soru -> DTO dönüşümü */
    private SoruDTO mapToSoruDTO(Soru s) {
        var konular = s.getKonular().stream()
                .map(k -> new KonuDTO(k.getId(), k.getAd(), k.getDokumanUrl(), k.getDokumanAdi(), k.getKonuAnlatimVideosuUrl()))
                .toList();

        var secenekler = secenekRepo.findBySoruOrderBySiralamaAscIdAsc(s).stream()
                .map(o -> new SecenekDTO(o.getId(), o.getMetin(), o.isDogru(), o.getSiralama()))
                .toList();

        return new SoruDTO(
                s.getId(),
                s.getMetin(),
                s.getTip(),
                s.getZorluk(),
                s.getImageUrl(),
                s.getDers().getAd(),
                konular,
                secenekler,
                s.getCozumVideosuUrl()
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
            for (DenemeSinaviSubmitItemDTO item : req.items()) {
                cevaplar.put(item.soruNo(), item.secilenCevap());
            }
        }

        // Her soru için kontrol et
        for (DenemeSinaviSoru soru : sorular) {
            String secilen = cevaplar.get(soru.getSoruNo());
            boolean bosmu = (secilen == null || secilen.trim().isEmpty());
            
            boolean dogru = false;
            if (!bosmu) {
                String dogruCevap = soru.getDogruCevap();
                dogru = dogruCevap != null && dogruCevap.trim().equalsIgnoreCase(secilen.trim());
            }

            if (bosmu) {
                empty++;
            } else if (dogru) {
                correct++;
            } else {
                wrong++;
            }
        }

        // YKS Net Hesaplama
        double net = correct - (wrong / 4.0);

        oturum.setTotal(total);
        oturum.setCorrect(correct);
        oturum.setWrong(wrong);
        oturum.setEmpty(empty);
        oturum.setScore((int) Math.round(net));
        oturumRepo.save(oturum);

        return new SubmitResponseDTO(oturum.getId(), correct, wrong, empty, total, net);
    }
}
