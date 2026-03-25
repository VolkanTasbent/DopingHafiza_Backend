package com.example.backend.controller;

import com.example.backend.dto.AiAbCompareResponseDTO;
import com.example.backend.dto.AiAnalyzeResponseDTO;
import com.example.backend.dto.AiChatRequestDTO;
import com.example.backend.dto.AiChatResponseDTO;
import com.example.backend.dto.AiSaveStudyPlanRequestDTO;
import com.example.backend.dto.AiSavedStudyPlanResponseDTO;
import com.example.backend.dto.AiStudyPlanResponseDTO;
import com.example.backend.dto.AiTrainingRowDTO;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.service.AiCoachService;
import com.example.backend.service.AiSavedStudyPlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class AiCoachController {

    private final AiCoachService aiCoachService;
    private final AiSavedStudyPlanService aiSavedStudyPlanService;
    private final AppUserRepository appUserRepository;

    public AiCoachController(
            AiCoachService aiCoachService,
            AiSavedStudyPlanService aiSavedStudyPlanService,
            AppUserRepository appUserRepository
    ) {
        this.aiCoachService = aiCoachService;
        this.aiSavedStudyPlanService = aiSavedStudyPlanService;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/analyze-weak-topics")
    public ResponseEntity<?> analyzeWeakTopics(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(defaultValue = "8") Integer limit,
            Principal principal
    ) {
        AppUser user = getCurrentUser(principal);
        if (user == null) return ResponseEntity.status(401).body("Kullanici bulunamadi.");

        AiAnalyzeResponseDTO response = aiCoachService.analyzeWeakTopics(user, days, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggest-study-plan")
    public ResponseEntity<?> suggestStudyPlan(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(defaultValue = "120") Integer dailyMinutes,
            @RequestParam(defaultValue = "mixed") String mode,
            Principal principal
    ) {
        AppUser user = getCurrentUser(principal);
        if (user == null) return ResponseEntity.status(401).body("Kullanici bulunamadi.");

        AiStudyPlanResponseDTO response = aiCoachService.suggestStudyPlan(user, days, dailyMinutes, mode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody AiChatRequestDTO request, Principal principal) {
        AppUser user = getCurrentUser(principal);
        if (user == null) return ResponseEntity.status(401).body("Kullanici bulunamadi.");

        AiChatResponseDTO response = aiCoachService.chat(user, request.message());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ab-compare")
    public ResponseEntity<?> abCompare(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(defaultValue = "8") Integer limit,
            Principal principal
    ) {
        AppUser user = getCurrentUser(principal);
        if (user == null) return ResponseEntity.status(401).body("Kullanici bulunamadi.");

        AiAbCompareResponseDTO response = aiCoachService.compareMlVsHeuristic(user, days, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/training-dataset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AiTrainingRowDTO>> trainingDataset(
            @RequestParam(defaultValue = "120") Integer days,
            @RequestParam(defaultValue = "12") Integer minAnswers
    ) {
        return ResponseEntity.ok(aiCoachService.exportTrainingDataset(days, minAnswers));
    }

    @GetMapping("/saved-study-plans")
    public ResponseEntity<?> listSavedStudyPlans(Principal principal) {
        AppUser user = getCurrentUser(principal);
        if (user == null) return ResponseEntity.status(401).body("Kullanici bulunamadi.");
        List<AiSavedStudyPlanResponseDTO> list = aiSavedStudyPlanService.listForUser(user);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/saved-study-plans")
    public ResponseEntity<?> saveStudyPlan(@Valid @RequestBody AiSaveStudyPlanRequestDTO body, Principal principal) {
        AppUser user = getCurrentUser(principal);
        if (user == null) return ResponseEntity.status(401).body("Kullanici bulunamadi.");
        AiSavedStudyPlanResponseDTO saved = aiSavedStudyPlanService.save(user, body);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/saved-study-plans/{id}")
    public ResponseEntity<?> deleteSavedStudyPlan(@PathVariable Long id, Principal principal) {
        AppUser user = getCurrentUser(principal);
        if (user == null) return ResponseEntity.status(401).build();
        boolean ok = aiSavedStudyPlanService.delete(user, id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private AppUser getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return appUserRepository.findByEmail(principal.getName()).orElse(null);
    }
}
