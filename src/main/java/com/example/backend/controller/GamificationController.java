package com.example.backend.controller;

import com.example.backend.dto.GamificationDtos.*;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.service.GamificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/gamification")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class GamificationController {

    private final AppUserRepository userRepository;
    private final GamificationService gamificationService;

    public GamificationController(AppUserRepository userRepository, GamificationService gamificationService) {
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
    }

    private AppUser requireUser(Principal principal) {
        if (principal == null) throw new IllegalArgumentException("Giris yapilmamis");
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Kullanici bulunamadi"));
    }

    @GetMapping("/state")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GamificationStateDTO> getState(Principal principal) {
        AppUser user = requireUser(principal);
        return ResponseEntity.ok(gamificationService.getState(user));
    }

    @PostMapping("/sync")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GamificationSyncResponseDTO> sync(Principal principal) {
        AppUser user = requireUser(principal);
        return ResponseEntity.ok(gamificationService.sync(user));
    }

    @GetMapping("/market-items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> marketItems() {
        return ResponseEntity.ok(gamificationService.marketItems());
    }

    @GetMapping("/daily-tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GamificationStateDTO> getDailyTasks(Principal principal) {
        AppUser user = requireUser(principal);
        return ResponseEntity.ok(gamificationService.getState(user));
    }

    @PostMapping("/task-complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GamificationStateDTO> completeTask(
            @RequestBody GamificationTaskCompleteRequestDTO request,
            Principal principal
    ) {
        AppUser user = requireUser(principal);
        return ResponseEntity.ok(gamificationService.completeTask(user, request.taskId()));
    }

    @PostMapping("/purchase")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GamificationStateDTO> purchase(
            @RequestBody GamificationPurchaseRequestDTO request,
            Principal principal
    ) {
        AppUser user = requireUser(principal);
        return ResponseEntity.ok(gamificationService.purchase(user, request.itemId()));
    }
}
