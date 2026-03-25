package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequestDTO(
        @NotBlank(message = "Mesaj bos olamaz.")
        String message
) {}
