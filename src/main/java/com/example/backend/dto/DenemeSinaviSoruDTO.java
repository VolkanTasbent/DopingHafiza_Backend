package com.example.backend.dto;

import java.time.Instant;

public record DenemeSinaviSoruDTO(
    Long id,
    Long denemeSinaviId,
    Long dersId,
    String dersAd,
    Integer soruNo,
    String soruMetni,
    String sikA,
    String sikB,
    String sikC,
    String sikD,
    String sikE,
    String dogruCevap,
    Integer zorluk,
    String konular, // Virgülle ayrılmış
    String aciklama,
    Instant olusturmaTarihi
) {}

