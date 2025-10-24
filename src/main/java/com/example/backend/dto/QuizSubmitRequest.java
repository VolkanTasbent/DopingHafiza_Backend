package com.example.backend.dto;

import java.time.Instant;
import java.util.List;

public record QuizSubmitRequest(
        Long dersId,
        Instant startedAt,
        Instant finishedAt,
        List<QuizSubmitItemDTO> items
) {}
