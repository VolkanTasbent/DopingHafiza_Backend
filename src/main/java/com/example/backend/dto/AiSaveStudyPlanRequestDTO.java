package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AiSaveStudyPlanRequestDTO(
        @NotBlank(message = "Baslik bos olamaz.")
        @Size(max = 500)
        String title,
        String summary,
        Integer analyzedDays,
        Integer dailyMinutes,
        String mode,
        List<AiStudyTaskDTO> tasks,
        List<String> focusTips,
        List<String> weakTopicsPreview
) {}
