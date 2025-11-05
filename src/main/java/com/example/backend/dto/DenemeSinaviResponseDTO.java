package com.example.backend.dto;

import java.time.Instant;

/**
 * Frontend için deneme sınavı response DTO
 * Frontend'in beklediği format: { id, adi, kategori, ... }
 */
public record DenemeSinaviResponseDTO(
    Long id,
    String adi,        // Frontend'de "adi" bekleniyor
    String kategori,   // Frontend'de "kategori" bekleniyor (TYT/AYT)
    Instant olusturmaTarihi,
    String aciklama,
    Integer soruSayisi
) {
    // Backend DTO'dan frontend DTO'ya dönüşüm
    public static DenemeSinaviResponseDTO from(DenemeSinaviDTO dto) {
        return new DenemeSinaviResponseDTO(
            dto.id(),
            dto.ad(),           // "ad" -> "adi"
            dto.tip(),          // "tip" -> "kategori"
            dto.olusturmaTarihi(),
            dto.aciklama(),
            dto.soruSayisi()
        );
    }
}


