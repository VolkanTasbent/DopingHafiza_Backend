package com.example.backend.dto;

import java.util.List;

public record SoruDTO(
        Long id,
        String metin,
        String tip,
        Integer zorluk,
        String imageUrl,
        String dersAd,
        List<KonuDTO> konular,       // <— artık liste
        List<SecenekDTO> secenekler
) {}
