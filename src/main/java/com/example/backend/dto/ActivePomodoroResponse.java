package com.example.backend.dto;

import java.time.Instant;

public class ActivePomodoroResponse {
    private Long userId;
    private Integer duration; // Dakika cinsinden
    private Integer remainingSeconds; // Kalan saniye
    private Instant startedAt;
    private Instant expiresAt;
    private boolean isActive;

    public ActivePomodoroResponse() {
    }

    public ActivePomodoroResponse(Long userId, Integer duration, Integer remainingSeconds, 
                                   Instant startedAt, Instant expiresAt, boolean isActive) {
        this.userId = userId;
        this.duration = duration;
        this.remainingSeconds = remainingSeconds;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Integer remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}

