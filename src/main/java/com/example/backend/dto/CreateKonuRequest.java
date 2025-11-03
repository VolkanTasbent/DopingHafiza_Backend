package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateKonuRequest(
    @NotNull(message = "Ders ID gerekli")
    @Positive(message = "Ders ID pozitif olmalı")
    Long dersId,
    
    @NotBlank(message = "Konu adı boş olamaz")
    @Size(max = 150, message = "Konu adı maksimum 150 karakter olabilir")
    String ad
) {
    // Compact constructor - otomatik trim
    public CreateKonuRequest {
        if (ad != null) {
            ad = ad.trim();
        }
    }
}



