package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.service.QuizService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class QuizController {
    private final QuizService service;
    private final AppUserRepository userRepo;

    public QuizController(QuizService service, AppUserRepository userRepo) {
        this.service = service; this.userRepo = userRepo;
    }

    /** Normal soru çözme - Submit */
    @PostMapping("/submit")
    public SubmitResponseDTO submit(@RequestBody QuizSubmitRequest req, Principal principal) {
        AppUser user = null;
        if (principal != null && principal.getName() != null) {
            user = userRepo.findByEmail(principal.getName()).orElse(null);
        }
        return service.submit(req, user);
    }

    /** Deneme sınavı çözme - Submit */
    @PostMapping("/submit-deneme-sinavi")
    public SubmitResponseDTO submitDenemeSinavi(@RequestBody DenemeSinaviSubmitRequest req, Principal principal) {
        AppUser user = null;
        if (principal != null && principal.getName() != null) {
            user = userRepo.findByEmail(principal.getName()).orElse(null);
        }
        return service.submitDenemeSinavi(req, user);
    }
}