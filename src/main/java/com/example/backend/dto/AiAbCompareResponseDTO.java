package com.example.backend.dto;

import java.util.List;

public record AiAbCompareResponseDTO(
        int analyzedDays,
        boolean mlEnabled,
        boolean mlResponded,
        String modelVersion,
        List<AiAbTopicCompareDTO> topics
) {}
