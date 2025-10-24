package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.*;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    private final DersRepository dersRepo;
    private final SoruRepository soruRepo;
    private final SecenekRepository secenekRepo;
    private final QuizOturumuRepository oturumRepo;
    private final CevapRepository cevapRepo;

    public QuizService(
            DersRepository dersRepo,
            SoruRepository soruRepo,
            SecenekRepository secenekRepo,
            QuizOturumuRepository oturumRepo,
            CevapRepository cevapRepo
    ) {
        this.dersRepo = dersRepo;
        this.soruRepo = soruRepo;
        this.secenekRepo = secenekRepo;
        this.oturumRepo = oturumRepo;
        this.cevapRepo = cevapRepo;
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

        int total = 0, correct = 0, wrong = 0;

        if (req.items() != null) {
            for (QuizSubmitItemDTO it : req.items()) {
                total++;

                Soru soru = soruRepo.findById(it.soruId())
                        .orElseThrow(() -> new IllegalArgumentException("Soru yok: " + it.soruId()));

                Secenek secenek = null;
                boolean dogru = false;

                if (it.secenekId() != null) {
                    secenek = secenekRepo.findById(it.secenekId()).orElse(null);
                    if (secenek != null) {
                        dogru = secenek.isDogru();
                    }
                }

                // 🧩 Debug log
                System.out.println("🧠 SoruID=" + it.soruId() +
                        " | SeçenekID=" + it.secenekId() +
                        " | Dogru=" + dogru);

                if (dogru) correct++; else wrong++;

                Cevap c = new Cevap();
                c.setOturum(oturum);
                c.setSoru(soru);
                c.setSecenek(secenek);
                c.setDogru(dogru);
                cevapRepo.save(c);
            }
        }

        int score = correct * 3 - wrong;
        oturum.setTotal(total);
        oturum.setCorrect(correct);
        oturum.setWrong(wrong);
        oturum.setScore(score);
        oturumRepo.save(oturum);

        System.out.println("✅ Oturum " + oturum.getId() + " kaydedildi. Doğru=" + correct + " | Yanlış=" + wrong);

        return new SubmitResponseDTO(oturum.getId(), correct, wrong, total, score);
    }

    /** ✅ Kullanıcının rapor özetlerini listeler */
    @Transactional(readOnly = true)
    public List<RaporOzetDTO> listOzetForUser(AppUser user, Integer limit) {
        var page = oturumRepo.findByUser(
                user,
                org.springframework.data.domain.PageRequest.of(0, limit != null ? limit : 20)
        );
        return page.getContent().stream()
                .map(o -> new RaporOzetDTO(
                        o.getId(),
                        o.getFinishedAt(),
                        o.getTotal(),
                        o.getCorrect(),
                        o.getWrong(),
                        o.getDurationMs(),
                        o.getScore()
                ))
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
                .map(k -> new KonuDTO(k.getId(), k.getAd()))
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
                secenekler
        );
    }
}
