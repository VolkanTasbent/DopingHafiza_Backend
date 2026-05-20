package com.example.backend.dto;

import java.util.List;

public record SolvedQuestionsDTO(
        List<Long> soruIds,
        long total
) {}
