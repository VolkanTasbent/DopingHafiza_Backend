package com.example.backend.dto;

import jakarta.validation.constraints.Min;

public class UpdateVideoNoteRequest {
    private String noteText;

    @Min(value = 0, message = "Timestamp seconds must be >= 0")
    private Integer timestampSeconds;

    // Getters and Setters
    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public Integer getTimestampSeconds() {
        return timestampSeconds;
    }

    public void setTimestampSeconds(Integer timestampSeconds) {
        this.timestampSeconds = timestampSeconds;
    }
}








