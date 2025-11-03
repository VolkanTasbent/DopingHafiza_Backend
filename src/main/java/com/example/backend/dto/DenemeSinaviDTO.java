package com.example.backend.dto;

import java.time.Instant;

public record DenemeSinaviDTO(
    Long id,
    String ad,
    String tip, // "TYT" veya "AYT"
    Instant olusturmaTarihi,
    String aciklama,
    Integer soruSayisi
) {}

