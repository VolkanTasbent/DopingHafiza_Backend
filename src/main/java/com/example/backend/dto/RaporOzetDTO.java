package com.example.backend.dto;

import java.time.Instant;

public record RaporOzetDTO(
        Long oturumId,
        Instant finishedAt,
        Integer totalCount,
        Integer correctCount,
        Integer wrongCount,
        Integer emptyCount,
        Long durationMs,
        Double net
) {}
