package com.example.backend.dto;

public record ScoreBreakdown(
    int baseScore,
    int netBonus,
    int activityBonus,
    int streakBonus,
    int accuracyBonus
) {}





