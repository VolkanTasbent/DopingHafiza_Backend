package com.example.backend.dto;

public record AuthUserDTO(
    Long id, 
    String email, 
    String ad, 
    String soyad, 
    String role, 
    String avatarUrl, 
    Integer hedefSiralama,
    String hedefUniversite,
    String hedefBolum,
    java.math.BigDecimal hedefPuan,
    Boolean darkMode
) {}
