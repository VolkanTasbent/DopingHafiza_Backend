package com.example.backend.controller;

import com.example.backend.dto.AuthUserDTO;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class AdminController {

    private final AppUserRepository repo;

    public AdminController(AppUserRepository repo) { this.repo = repo; }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public AuthUserDTO setRole(@PathVariable Long id, @RequestParam String role) {
        role = role.toUpperCase();
        if (!role.equals("USER") && !role.equals("ADMIN"))
            throw new IllegalArgumentException("Geçersiz rol: " + role);

        AppUser u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Kullanıcı yok: " + id));
        u.setRole(role);
        repo.save(u);
        return new AuthUserDTO(u.getId(), u.getEmail(), u.getAd(), u.getSoyad(), u.getRole(), u.getAvatarUrl(), 
                u.getHedefSiralama(), u.getHedefUniversite(), u.getHedefBolum(), u.getHedefPuan(),
                u.getDarkMode() != null ? u.getDarkMode() : false,
                u.getPuan(),
                u.getAltin(),
                u.getGamificationState());
    }
}
