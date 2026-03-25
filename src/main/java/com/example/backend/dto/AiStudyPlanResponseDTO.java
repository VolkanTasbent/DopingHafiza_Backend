package com.example.backend.dto;

import java.util.List;

public record AiStudyPlanResponseDTO(
        int analyzedDays,
        int dailyMinutes,
        String mode,
        List<AiStudyTaskDTO> tasks,
        String summary
) {}
