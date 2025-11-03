package com.example.backend.dto;

public record DenemeSinaviSubmitItemDTO(
    Integer soruNo,  // Deneme sınavındaki soru numarası
    String secilenCevap  // "A", "B", "C", "D", "E" veya null (boş)
) {}

