package com.example.backend.dto;

public record UserStats(
    int totalCorrect,
    int totalWrong,
    int totalEmpty,
    double totalNet,
    double accuracy,
    int totalReports
) {}




