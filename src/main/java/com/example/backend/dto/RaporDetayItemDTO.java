package com.example.backend.dto;

public record RaporDetayItemDTO(
        Long id,                 // cevap id
        SoruDTO soru,
        Long secenekId,          // kullanıcının seçtiği
        Boolean dogru
) {}
