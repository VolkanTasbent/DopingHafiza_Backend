package com.example.backend.dto;

import java.util.List;

public record AiAnalyzeResponseDTO(
        int analyzedDays,
        int totalAnswers,
        double overallSuccessRate,
        List<AiWeakTopicDTO> weakTopics,
        List<String> focusTips
) {}
