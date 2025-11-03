package com.example.backend.dto;

public record DenemeSinaviSoruDTOForQuiz(
    Long id,
    Long dersId,
    String dersAd,
    Integer soruNo,
    String soruMetni,
    String sikA,
    String sikB,
    String sikC,
    String sikD,
    String sikE,
    Integer zorluk,
    String konular
) {}

