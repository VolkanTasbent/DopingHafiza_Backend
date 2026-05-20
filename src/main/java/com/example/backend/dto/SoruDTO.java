package com.example.backend.dto;

import java.time.Instant;
import java.util.List;

public record SoruDTO(
        Long id,
        String metin,
        String tip,
        Integer zorluk,
        String imageUrl,
        String dersAd,
        List<KonuDTO> konular,
        List<SecenekDTO> secenekler,
        String cozumVideosuUrl,
        Boolean cozuldu,
        Instant cozulduAt,
        Boolean bosBirakildi
) {}
