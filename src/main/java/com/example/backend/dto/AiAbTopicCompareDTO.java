package com.example.backend.dto;

public record AiAbTopicCompareDTO(
        Long dersId,
        String dersAd,
        Long konuId,
        String konuAd,
        double heuristicRisk,
        Double mlRisk,
        Double delta,
        String activeSource,
        String modelVersion
) {}
