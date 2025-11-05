package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Frontend'den gelen request DTO
 * Frontend'in gönderdiği format: { adi: "...", kategori: "TYT" }
 */
public record CreateDenemeSinaviRequestFrontend(
    @NotBlank(message = "Ad boş olamaz")
    @Size(max = 200, message = "Ad maksimum 200 karakter olabilir")
    String adi,        // Frontend'den "adi" geliyor
    
    @NotBlank(message = "Kategori boş olamaz")
    @Pattern(regexp = "^(TYT|AYT)$", message = "Kategori sadece TYT veya AYT olabilir")
    String kategori,   // Frontend'den "kategori" geliyor
    
    @Size(max = 1000, message = "Açıklama maksimum 1000 karakter olabilir")
    String aciklama
) {
    // Frontend request'ten backend request'e dönüşüm
    public CreateDenemeSinaviRequest toBackendRequest() {
        return new CreateDenemeSinaviRequest(
            this.adi(),      // "adi" -> "ad"
            this.kategori(), // "kategori" -> "tip"
            this.aciklama()
        );
    }
}


