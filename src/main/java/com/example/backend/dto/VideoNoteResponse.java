package com.example.backend.dto;

import com.example.backend.model.VideoNote;
import java.time.Instant;

public class VideoNoteResponse {
    private Long id;
    private Long userId;
    private Long konuId;
    private String videoId;
    private String videoUrl;
    private String noteText;
    private Integer timestampSeconds;
    private String timestampFormatted;
    private Instant createdAt;
    private Instant updatedAt;

    public static VideoNoteResponse from(VideoNote note) {
        VideoNoteResponse response = new VideoNoteResponse();
        response.setId(note.getId());
        response.setUserId(note.getUserId());
        response.setKonuId(note.getKonuId());
        response.setVideoId(note.getVideoId());
        response.setVideoUrl(note.getVideoUrl());
        response.setNoteText(note.getNoteText());
        response.setTimestampSeconds(note.getTimestampSeconds());
        response.setTimestampFormatted(formatTimestamp(note.getTimestampSeconds()));
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        return response;
    }

    private static String formatTimestamp(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%d:%02d", minutes, secs);
    }

    // Getters and Setters
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

    public Long getKonuId() {
        return konuId;
    }

    public void setKonuId(Long konuId) {
        this.konuId = konuId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
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

    public String getTimestampFormatted() {
        return timestampFormatted;
    }

    public void setTimestampFormatted(String timestampFormatted) {
        this.timestampFormatted = timestampFormatted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}






