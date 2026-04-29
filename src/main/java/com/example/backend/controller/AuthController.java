package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public AuthUserDTO register(@RequestBody RegisterRequest body) {
        return authService.register(body);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest body) {
        return authService.login(body);
    }
}
