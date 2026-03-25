package com.example.backend.dto;

import java.util.List;

public record AiChatResponseDTO(
        String answer,
        List<String> quickReplies
) {}
