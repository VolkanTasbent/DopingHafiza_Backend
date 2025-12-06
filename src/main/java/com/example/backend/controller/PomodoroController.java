package com.example.backend.controller;

import com.example.backend.dto.PomodoroSessionRequest;
import com.example.backend.dto.PomodoroSessionResponse;
import com.example.backend.dto.PomodoroStatsResponse;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.service.PomodoroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pomodoro")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class PomodoroController {

    private final PomodoroService pomodoroService;
    private final AppUserRepository userRepository;

    public PomodoroController(PomodoroService pomodoroService, AppUserRepository userRepository) {
        this.pomodoroService = pomodoroService;
        this.userRepository = userRepository;
    }

    @PostMapping("/session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PomodoroSessionResponse> saveSession(
            @Valid @RequestBody PomodoroSessionRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        PomodoroSessionResponse response = pomodoroService.saveSession(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PomodoroStatsResponse> getStats(Authentication authentication) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        PomodoroStatsResponse stats = pomodoroService.getStats(user.getId());
        return ResponseEntity.ok(stats);
    }
}


