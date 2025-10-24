package com.example.backend.controller;

import com.example.backend.dto.RaporDetayDTO;
import com.example.backend.dto.RaporOzetDTO;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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

    // 🔹 1. Oturum listesini getir
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

    // 🔹 2. Oturum detayı
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
}
