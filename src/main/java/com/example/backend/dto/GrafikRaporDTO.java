package com.example.backend.dto;

import java.time.Instant;
import java.util.List;

// Sadece grafikler için özel DTO
public record GrafikRaporDTO(
        Long oturumId,
        Instant finishedAt,  // Instant olarak değiştir
        Integer correctCount,
        Integer wrongCount,
        Integer emptyCount,
        Long durationMs,
        Double net,
        List<RaporDetayItemDTO> items
) {}