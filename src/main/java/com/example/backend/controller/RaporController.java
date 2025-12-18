package com.example.backend.controller;

import com.example.backend.dto.DailyStudyTime;
import com.example.backend.dto.DailyStudyTimesResponse;
import com.example.backend.dto.GrafikRaporDTO;
import com.example.backend.dto.RaporDetayDTO;
import com.example.backend.dto.RaporDetayItemDTO;
import com.example.backend.dto.RaporOzetDTO;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.service.QuizService;
import com.example.backend.service.StudyTimeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/raporlar")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class RaporController {

    private final QuizService quizService;
    private final AppUserRepository userRepo;
    private final StudyTimeService studyTimeService;

    public RaporController(QuizService quizService, AppUserRepository userRepo, StudyTimeService studyTimeService) {
        this.quizService = quizService;
        this.userRepo = userRepo;
        this.studyTimeService = studyTimeService;
    }

    // 🔹 1. Oturum listesini getir (MEVCUT - AYNI KALDI)
    @GetMapping
    public ResponseEntity<?> getRaporlar(
            @RequestParam(defaultValue = "20") Integer limit,
            Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(401).body("Giriş yapılmamış.");
        AppUser user = userRepo.findByEmail(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Kullanıcı bulunamadı.");

        List<RaporOzetDTO> raporlar = quizService.listOzetForUser(user, limit);
        return ResponseEntity.ok(raporlar);
    }

    // 🔹 2. Oturum detayı (MEVCUT - AYNI KALDI)
    @GetMapping("/{oturumId}/detay")
    public ResponseEntity<?> getDetay(@PathVariable Long oturumId, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Giriş yapılmamış.");
        AppUser user = userRepo.findByEmail(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Kullanıcı bulunamadı.");

        try {
            RaporDetayDTO detay = quizService.detayForUser(user, oturumId, false);
            return ResponseEntity.ok(detay);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Detay alınamadı: " + e.getMessage());
        }
    }

    // 🔹 3. YENİ: Grafikler için özel endpoint - DETAYLI DEBUG EKLENDİ
    // RaporController.java - dogru field'ını kullanacak şekilde güncellendi
    @GetMapping("/grafikler")
    public ResponseEntity<?> getRaporlarForGrafikler(
            @RequestParam(defaultValue = "50") Integer limit,
            Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(401).body("Giriş yapılmamış.");
        AppUser user = userRepo.findByEmail(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Kullanıcı bulunamadı.");

        try {
            List<GrafikRaporDTO> grafikRaporlari = quizService.listOzetForUser(user, limit)
                    .stream()
                    .map(ozet -> {
                        try {
                            System.out.println("\n🔄 ========================================");
                            System.out.println("🔄 Grafikler için oturum detayı alınıyor: " + ozet.oturumId());
                            System.out.println("📊 Özet Bilgiler - Doğru: " + ozet.correctCount() +
                                    ", Yanlış: " + ozet.wrongCount() +
                                    ", Boş: " + ozet.emptyCount());

                            // ✅ Items verisini detayForUser metodundan al
                            RaporDetayDTO detay = quizService.detayForUser(user, ozet.oturumId(), false);

                            // DETAYLI DEBUG: Her item için doğru/yanlış bilgisini kontrol et
                            if (detay.items() != null && !detay.items().isEmpty()) {
                                System.out.println("📦 Oturum " + ozet.oturumId() + " - Items sayısı: " + detay.items().size());

                                int dogruSayisi = 0;
                                int yanlisSayisi = 0;
                                int bosSayisi = 0;

                                // İlk 5 item'ı detaylı incele
                                for (int i = 0; i < Math.min(detay.items().size(), 5); i++) {
                                    RaporDetayItemDTO item = detay.items().get(i);
                                    String durum = "Bilinmiyor";

                                    if (item.dogru() == null) {
                                        bosSayisi++;
                                        durum = "BOŞ (dogru: null)";
                                    } else if (item.dogru()) {
                                        dogruSayisi++;
                                        durum = "DOĞRU";
                                    } else {
                                        yanlisSayisi++;
                                        durum = "YANLIŞ";
                                    }

                                    System.out.println("   " + (i + 1) + ". Item ID: " + item.id() +
                                            " | Ders: " + (item.soru().dersAd() != null ? item.soru().dersAd() : "null") +
                                            " | Durum: " + durum +
                                            " | SecenekID: " + item.secenekId() +
                                            " | dogru field: " + item.dogru());
                                }

                                // Kalan item'lar için toplu sayım
                                for (int i = 5; i < detay.items().size(); i++) {
                                    RaporDetayItemDTO item = detay.items().get(i);
                                    if (item.dogru() == null) {
                                        bosSayisi++;
                                    } else if (item.dogru()) {
                                        dogruSayisi++;
                                    } else {
                                        yanlisSayisi++;
                                    }
                                }

                                System.out.println("📊 Oturum " + ozet.oturumId() + " ITEMS SONUÇ: " +
                                        dogruSayisi + "✅ " + yanlisSayisi + "❌ " + bosSayisi + "⚪");

                                // Özet ile items karşılaştırması
                                System.out.println("🔍 KARŞILAŞTIRMA - Özet: " + ozet.correctCount() + "✅ " +
                                        ozet.wrongCount() + "❌ " + ozet.emptyCount() + "⚪" +
                                        " vs Items: " + dogruSayisi + "✅ " + yanlisSayisi + "❌ " + bosSayisi + "⚪");

                            } else {
                                System.out.println("❌ Oturum " + ozet.oturumId() + " için items BOŞ veya NULL!");
                            }

                            System.out.println("✅ Oturum " + ozet.oturumId() + " işlendi");
                            System.out.println("========================================\n");

                            // GrafikRaporDTO'ya dönüştür - ITEMS EKLENDİ
                            return new GrafikRaporDTO(
                                    detay.oturumId(),
                                    ozet.finishedAt(),
                                    ozet.correctCount(),
                                    ozet.wrongCount(),
                                    ozet.emptyCount(),
                                    ozet.durationMs(),
                                    ozet.net(),
                                    detay.items() != null ? detay.items() : List.of()
                            );
                        } catch (Exception e) {
                            System.out.println("❌❌❌ DETAY ALINAMADI oturumId: " + ozet.oturumId() + " - " + e.getMessage());
                            e.printStackTrace();
                            return new GrafikRaporDTO(
                                    ozet.oturumId(),
                                    ozet.finishedAt(),
                                    ozet.correctCount(),
                                    ozet.wrongCount(),
                                    ozet.emptyCount(),
                                    ozet.durationMs(),
                                    ozet.net(),
                                    List.of()
                            );
                        }
                    })
                    .collect(Collectors.toList());

            // Toplam items istatistikleri
            long itemsOlanRaporlar = grafikRaporlari.stream()
                    .filter(r -> r.items() != null && !r.items().isEmpty())
                    .count();

            long toplamItems = grafikRaporlari.stream()
                    .filter(r -> r.items() != null)
                    .mapToLong(r -> r.items().size())
                    .sum();

            long toplamDogruItems = grafikRaporlari.stream()
                    .filter(r -> r.items() != null)
                    .flatMap(r -> r.items().stream())
                    .filter(item -> item.dogru() != null && item.dogru())
                    .count();

            long toplamYanlisItems = grafikRaporlari.stream()
                    .filter(r -> r.items() != null)
                    .flatMap(r -> r.items().stream())
                    .filter(item -> item.dogru() != null && !item.dogru())
                    .count();

            long toplamBosItems = grafikRaporlari.stream()
                    .filter(r -> r.items() != null)
                    .flatMap(r -> r.items().stream())
                    .filter(item -> item.dogru() == null)
                    .count();

            System.out.println("🎯🎯🎯 GRAFİK ENDPOINT SONUÇ RAPORU 🎯🎯🎯");
            System.out.println("📊 Toplam Rapor: " + grafikRaporlari.size());
            System.out.println("📦 Items İçeren Rapor: " + itemsOlanRaporlar);
            System.out.println("🔢 Toplam Items: " + toplamItems);
            System.out.println("✅ Toplam DOĞRU Items: " + toplamDogruItems);
            System.out.println("❌ Toplam YANLIŞ Items: " + toplamYanlisItems);
            System.out.println("⚪ Toplam BOŞ Items: " + toplamBosItems);
            System.out.println("🎯🎯🎯 SONUÇ RAPORU TAMAMLANDI 🎯🎯🎯");

            return ResponseEntity.ok(grafikRaporlari);
        } catch (Exception e) {
            System.out.println("❌❌❌ GRAFİK VERİLERİ ALINAMADI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Grafik verileri alınamadı: " + e.getMessage());
        }
    }

    // 🔹 4. YENİ: Günlük çalışma süreleri getir (Soru çözme + Pomodoro birleştirilmiş)
    @GetMapping("/daily-study-times")
    public ResponseEntity<?> getDailyStudyTimes(
            @RequestParam(required = false, defaultValue = "10") Integer days,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Principal principal
    ) {
        if (principal == null) return ResponseEntity.status(401).body("Giriş yapılmamış.");
        AppUser user = userRepo.findByEmail(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Kullanıcı bulunamadı.");

        try {
            // Tarih aralığını belirle
            Instant end = endDate != null ? endDate.atZone(ZoneId.systemDefault()).toInstant() : Instant.now();
            Instant start = startDate != null ? startDate.atZone(ZoneId.systemDefault()).toInstant() : end.minusSeconds(days * 24 * 60 * 60);

            List<DailyStudyTime> dailyTimes = studyTimeService.getDailyStudyTimes(user, start, end);

            return ResponseEntity.ok(new DailyStudyTimesResponse(dailyTimes));
        } catch (Exception e) {
            System.err.println("❌ Günlük çalışma süreleri alınamadı: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Günlük çalışma süreleri alınamadı: " + e.getMessage());
        }
    }
}