package com.example.backend.dto;

import java.time.Instant;
import java.util.List;

public record DenemeSinaviSubmitRequest(
    Long denemeSinaviId,
    Instant startedAt,
    Instant finishedAt,
    List<DenemeSinaviSubmitItemDTO> items // soruNo -> seçilen şık (A, B, C, D, E veya null)
) {}

