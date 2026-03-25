package com.example.backend.dto;

public record AiTrainingRowDTO(
        Long userId,
        Long dersId,
        String dersAd,
        Long konuId,
        String konuAd,
        int totalCount,
        int correctCount,
        int wrongCount,
        int blankCount,
        double successRate,
        double wrongRate,
        double blankRate,
        double volume,
        int riskHigh
) {}
