package com.example.backend.controller;

import com.example.backend.dto.GrafikRaporDTO;
import com.example.backend.dto.RaporDetayDTO;
import com.example.backend.dto.RaporOzetDTO;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/raporlar")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class RaporController {

    private final QuizService quizService;
    private final AppUserRepository userRepo;

    public RaporController(QuizService quizService, AppUserRepository userRepo) {
        this.quizService = quizService;
        this.userRepo = userRepo;
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

    // 🔹 3. YENİ: Grafikler için özel endpoint
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
                            RaporDetayDTO detay = quizService.detayForUser(user, ozet.oturumId(), false);

                            // GrafikRaporDTO'ya dönüştür - artık Instant uyumlu
                            return new GrafikRaporDTO(
                                    detay.oturumId(),
                                    ozet.finishedAt(), // Instant olarak doğrudan al
                                    ozet.correctCount(),
                                    ozet.wrongCount(),
                                    ozet.emptyCount(),
                                    ozet.durationMs(),
                                    ozet.net(),
                                    detay.items()
                            );
                        } catch (Exception e) {
                            System.out.println("Detay alınamadı oturumId: " + ozet.oturumId() + " - " + e.getMessage());

                            // Detay alınamazsa sadece özet bilgileri ile oluştur
                            return new GrafikRaporDTO(
                                    ozet.oturumId(),
                                    ozet.finishedAt(),
                                    ozet.correctCount(),
                                    ozet.wrongCount(),
                                    ozet.emptyCount(),
                                    ozet.durationMs(),
                                    ozet.net(),
                                    List.of() // Boş items
                            );
                        }
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(grafikRaporlari);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Grafik verileri alınamadı: " + e.getMessage());
        }
    }
}