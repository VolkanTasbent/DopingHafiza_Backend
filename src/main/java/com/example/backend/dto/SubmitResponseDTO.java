package com.example.backend.dto;

public record SubmitResponseDTO(Long oturumId, int correct, int wrong, int total, int score) {}
