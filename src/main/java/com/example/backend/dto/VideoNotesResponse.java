package com.example.backend.dto;

import java.util.List;

public class VideoNotesResponse {
    private List<VideoNoteResponse> notes;

    public VideoNotesResponse() {
    }

    public VideoNotesResponse(List<VideoNoteResponse> notes) {
        this.notes = notes;
    }

    public List<VideoNoteResponse> getNotes() {
        return notes;
    }

    public void setNotes(List<VideoNoteResponse> notes) {
        this.notes = notes;
    }
}








