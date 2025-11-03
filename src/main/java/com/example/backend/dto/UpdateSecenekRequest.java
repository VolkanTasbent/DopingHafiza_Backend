package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSecenekRequest(
    Long id,  // null ise yeni seçenek, var ise güncelleme
    
    @NotBlank(message = "Seçenek metni boş olamaz")
    @Size(max = 500, message = "Seçenek metni maksimum 500 karakter olabilir")
    String metin,  // Güncelleme için zorunlu
    
    Boolean dogru,  // null ise güncellenmez (güncelleme için)
    
    Integer siralama  // null ise güncellenmez
) {
    // Compact constructor
    public UpdateSecenekRequest {
        if (metin != null) {
            metin = metin.trim();
        }
    }
}

