package com.example.backend.dto;

import java.time.LocalDate;

public record StreakDTO(
    int currentStreak,
    int longestStreak,
    LocalDate lastActivityDate
) {}




