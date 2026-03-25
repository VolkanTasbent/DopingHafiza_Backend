package com.example.backend.dto;

public record AiStudyTaskDTO(
        String taskType,
        String title,
        String description,
        int estimatedMinutes,
        int priority
) {}
