package com.example.backend.dto;

public record ScoreCalculationResponse(
    Long userId,
    int oldScore,
    int newScore,
    ScoreBreakdown breakdown,
    UserStats stats
) {}





