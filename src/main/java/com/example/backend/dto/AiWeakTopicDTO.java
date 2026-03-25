package com.example.backend.dto;

public record AiWeakTopicDTO(
        Long dersId,
        String dersAd,
        Long konuId,
        String konuAd,
        int totalCount,
        int correctCount,
        int wrongCount,
        int blankCount,
        double successRate,
        double riskScore,
        String recommendation,
        String source,
        String modelVersion
) {}
