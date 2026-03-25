package com.example.backend.dto;

import java.time.Instant;
import java.util.List;

public record AiSavedStudyPlanResponseDTO(
        Long id,
        Instant savedAt,
        String title,
        String summary,
        Integer analyzedDays,
        Integer dailyMinutes,
        String mode,
        List<AiStudyTaskDTO> tasks,
        List<String> focusTips,
        List<String> weakTopicsPreview
) {}
