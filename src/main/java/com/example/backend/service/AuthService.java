package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthService(AppUserRepository repo, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwt) {
        this.repo = repo; this.encoder = encoder; this.authManager = authManager; this.jwt = jwt;
    }

    public AuthUserDTO register(    RegisterRequest r) {
        if (repo.existsByEmail(r.email())) {
            throw new IllegalArgumentException("Email zaten kayıtlı");
        }
        AppUser u = new AppUser();
        u.setEmail(r.email());
        u.setAd(r.ad());
        u.setSoyad(r.soyad());
        u.setPassword(encoder.encode(r.password()));
        u.setRole("USER");
        u.setEnabled(true);
        u.setAvatarUrl(null); // Başlangıçta profil resmi yok
        u.setHedefSiralama(r.hedefSiralama()); // Hedef sıralama
        u.setHedefUniversite(r.hedefUniversite()); // Hedef üniversite
        u.setHedefBolum(r.hedefBolum()); // Hedef bölüm
        u.setHedefPuan(r.hedefPuan()); // Hedef puan
        u = repo.save(u);
        return new AuthUserDTO(u.getId(), u.getEmail(), u.getAd(), u.getSoyad(), u.getRole(), u.getAvatarUrl(), 
                u.getHedefSiralama(), u.getHedefUniversite(), u.getHedefBolum(), u.getHedefPuan(),
                u.getDarkMode() != null ? u.getDarkMode() : false,
                u.getPuan(),
                u.getAltin(),
                u.getGamificationState());
    }

    public LoginResponse login(LoginRequest r) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(r.email(), r.password()));
        // Başarılı ise token üret
        String token = jwt.generate(r.email());
        AppUser u = repo.findByEmail(r.email()).orElseThrow();
        var userDto = new AuthUserDTO(u.getId(), u.getEmail(), u.getAd(), u.getSoyad(), u.getRole(), u.getAvatarUrl(), 
                u.getHedefSiralama(), u.getHedefUniversite(), u.getHedefBolum(), u.getHedefPuan(),
                u.getDarkMode() != null ? u.getDarkMode() : false,
                u.getPuan(),
                u.getAltin(),
                u.getGamificationState());
        return new LoginResponse(token, userDto);
    }
}
