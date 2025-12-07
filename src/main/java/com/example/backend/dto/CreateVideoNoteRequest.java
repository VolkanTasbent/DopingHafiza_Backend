package com.example.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateVideoNoteRequest {
    @NotNull(message = "Konu ID is required")
    private Long konuId;

    @NotBlank(message = "Video URL is required")
    private String videoUrl;

    @NotBlank(message = "Note text is required")
    private String noteText;

    @NotNull(message = "Timestamp seconds is required")
    @Min(value = 0, message = "Timestamp seconds must be >= 0")
    private Integer timestampSeconds;

    // Getters and Setters
    public Long getKonuId() {
        return konuId;
    }

    public void setKonuId(Long konuId) {
        this.konuId = konuId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

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

