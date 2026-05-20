package com.example.backend.controller;

import com.example.backend.dto.AuthUserDTO;
import com.example.backend.dto.SolvedQuestionCountDTO;
import com.example.backend.dto.SolvedQuestionsDTO;
import com.example.backend.dto.UpdateUserDTO;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.service.UserSolvedQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class UsersController {

    private final AppUserRepository repo;
    private final UserSolvedQuestionService solvedQuestionService;

    public UsersController(AppUserRepository repo, UserSolvedQuestionService solvedQuestionService) {
        this.repo = repo;
        this.solvedQuestionService = solvedQuestionService;
    }

    private AppUser requireUser(Authentication auth) {
        var email = auth != null ? auth.getName() : null;
        return repo.findByEmail(email).orElseThrow();
    }

    @GetMapping("/me/solved-questions")
    @PreAuthorize("isAuthenticated()")
    public SolvedQuestionsDTO mySolvedQuestions(
            Authentication auth,
            @RequestParam(required = false) Long dersId,
            @RequestParam(required = false) Long konuId
    ) {
        AppUser user = requireUser(auth);
        return solvedQuestionService.listSolved(user.getId(), dersId, konuId);
    }

    @GetMapping("/me/solved-questions/count")
    @PreAuthorize("isAuthenticated()")
    public SolvedQuestionCountDTO mySolvedQuestionCount(
            Authentication auth,
            @RequestParam(required = false) Long dersId,
            @RequestParam(required = false) Long konuId
    ) {
        AppUser user = requireUser(auth);
        return solvedQuestionService.countSolved(user.getId(), dersId, konuId);
    }

    @GetMapping("/me")
    public AuthUserDTO me(Authentication auth) {
        var email = auth != null ? auth.getName() : null;
        var u = repo.findByEmail(email).orElseThrow();
        
        // Rol bilgisi sadece ADMIN'lere gösterilir
        String roleToShow = null;
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            roleToShow = u.getRole();
        }
        
        return new AuthUserDTO(u.getId(), u.getEmail(), u.getAd(), u.getSoyad(), roleToShow, u.getAvatarUrl(), 
                u.getHedefSiralama(), u.getHedefUniversite(), u.getHedefBolum(), u.getHedefPuan(), 
                u.getDarkMode() != null ? u.getDarkMode() : false,
                u.getPuan(),
                u.getAltin(),
                u.getGamificationState());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthUserDTO> updateProfile(@RequestBody UpdateUserDTO dto, Authentication auth) {
        String email = auth.getName();
        AppUser user = repo.findByEmail(email).orElseThrow();
        
        // Ad, soyad, email güncellemesi
        if (dto.getAd() != null && !dto.getAd().isBlank()) {
            user.setAd(dto.getAd());
        }
        if (dto.getSoyad() != null && !dto.getSoyad().isBlank()) {
            user.setSoyad(dto.getSoyad());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail());
        }
        
        // Avatar URL güncellemesi
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        
        // Hedef sıralama güncellemesi
        if (dto.getHedefSiralama() != null) {
            user.setHedefSiralama(dto.getHedefSiralama());
        }
        
        // Hedef üniversite güncellemesi
        if (dto.getHedefUniversite() != null) {
            user.setHedefUniversite(dto.getHedefUniversite());
        }
        
        // Hedef bölüm güncellemesi
        if (dto.getHedefBolum() != null) {
            user.setHedefBolum(dto.getHedefBolum());
        }
        
        // Hedef puan güncellemesi
        if (dto.getHedefPuan() != null) {
            user.setHedefPuan(dto.getHedefPuan());
        }
        
        // Dark mode güncellemesi
        if (dto.getDarkMode() != null) {
            user.setDarkMode(dto.getDarkMode());
        }

        // Puan güncellemesi (gamification sistemi)
        if (dto.getPuan() != null) {
            user.setPuan(Math.max(0, dto.getPuan()));
        }

        // Altın güncellemesi (gamification sistemi)
        if (dto.getAltin() != null) {
            user.setAltin(Math.max(0, dto.getAltin()));
        }

        // Kalici oyun durumu JSON
        if (dto.getGamificationState() != null) {
            user.setGamificationState(dto.getGamificationState());
        }
        
        AppUser savedUser = repo.save(user);
        
        // Rol bilgisi sadece ADMIN'lere gösterilir
        String roleToShow = null;
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            roleToShow = savedUser.getRole();
        }
        
        AuthUserDTO response = new AuthUserDTO(
            savedUser.getId(), 
            savedUser.getEmail(), 
            savedUser.getAd(), 
            savedUser.getSoyad(), 
            roleToShow, 
            savedUser.getAvatarUrl(),
            savedUser.getHedefSiralama(),
            savedUser.getHedefUniversite(),
            savedUser.getHedefBolum(),
            savedUser.getHedefPuan(),
            savedUser.getDarkMode() != null ? savedUser.getDarkMode() : false,
            savedUser.getPuan(),
            savedUser.getAltin(),
            savedUser.getGamificationState()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuthUserDTO> all() {
        return repo.findAll().stream()
                .map(u -> new AuthUserDTO(u.getId(), u.getEmail(), u.getAd(), u.getSoyad(), u.getRole(), u.getAvatarUrl(), 
                        u.getHedefSiralama(), u.getHedefUniversite(), u.getHedefBolum(), u.getHedefPuan(),
                        u.getDarkMode() != null ? u.getDarkMode() : false,
                        u.getPuan(),
                        u.getAltin(),
                        u.getGamificationState()))
                .toList();
    }
}
