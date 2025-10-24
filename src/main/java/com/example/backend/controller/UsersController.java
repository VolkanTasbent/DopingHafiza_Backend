package com.example.backend.controller;

import com.example.backend.dto.AuthUserDTO;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class UsersController {

    private final AppUserRepository repo;

    public UsersController(AppUserRepository repo) { this.repo = repo; }

    @GetMapping("/me")
    public AuthUserDTO me(Authentication auth) {
        var email = auth != null ? auth.getName() : null;
        var u = repo.findByEmail(email).orElseThrow();
        return new AuthUserDTO(u.getId(), u.getEmail(), u.getAd(), u.getSoyad(), u.getRole());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuthUserDTO> all() {
        return repo.findAll().stream()
                .map(u -> new AuthUserDTO(u.getId(), u.getEmail(), u.getAd(), u.getSoyad(), u.getRole()))
                .toList();
    }
}
