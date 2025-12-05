package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank String ad,
        @NotBlank String soyad,
        @Size(min = 6, message = "Şifre en az 6 karakter olmalı")
        String password,
        Integer hedefSiralama
) {}
