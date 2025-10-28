package com.example.backend.dto;

public record SubmitResponseDTO(Long oturumId, int correct, int wrong, int empty, int total, double net) {}
