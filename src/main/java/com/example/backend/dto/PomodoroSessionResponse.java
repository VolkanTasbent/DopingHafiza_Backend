package com.example.backend.dto;

import com.example.backend.model.PomodoroSession;

public class PomodoroSessionResponse {
    private Long id;
    private Long userId;
    private Integer duration;
    private String completedAt;
    private String createdAt;

    public static PomodoroSessionResponse from(PomodoroSession session) {
        PomodoroSessionResponse response = new PomodoroSessionResponse();
        response.setId(session.getId());
        response.setUserId(session.getUserId());
        response.setDuration(session.getDuration());
        response.setCompletedAt(session.getCompletedAt().toString());
        response.setCreatedAt(session.getCreatedAt().toString());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}









