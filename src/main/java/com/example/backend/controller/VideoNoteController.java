package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.model.AppUser;
import com.example.backend.model.VideoNote;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.VideoNoteRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/video-notes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class VideoNoteController {

    private final VideoNoteRepository videoNoteRepository;
    private final AppUserRepository userRepository;

    public VideoNoteController(VideoNoteRepository videoNoteRepository, AppUserRepository userRepository) {
        this.videoNoteRepository = videoNoteRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VideoNoteResponse> createVideoNote(
            @Valid @RequestBody CreateVideoNoteRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VideoNote note = new VideoNote();
        note.setUserId(user.getId());
        note.setKonuId(request.getKonuId());
        note.setVideoUrl(request.getVideoUrl());
        note.setNoteText(request.getNoteText());
        note.setTimestampSeconds(request.getTimestampSeconds());

        VideoNote saved = videoNoteRepository.save(note);

        return ResponseEntity.ok(VideoNoteResponse.from(saved));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VideoNotesResponse> getVideoNotes(
            @RequestParam(required = false) Long konuId,
            @RequestParam(required = false) String videoUrl,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<VideoNote> notes;
        if (konuId != null) {
            notes = videoNoteRepository.findByUserIdAndKonuIdOrderByTimestampSecondsAsc(user.getId(), konuId);
        } else if (videoUrl != null) {
            notes = videoNoteRepository.findByUserIdAndVideoUrlOrderByTimestampSecondsAsc(user.getId(), videoUrl);
        } else {
            notes = videoNoteRepository.findByUserIdOrderByTimestampSecondsAsc(user.getId());
        }

        List<VideoNoteResponse> noteResponses = notes.stream()
                .map(VideoNoteResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new VideoNotesResponse(noteResponses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VideoNoteResponse> updateVideoNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVideoNoteRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VideoNote note = videoNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video note not found"));

        // Sadece kendi notunu güncelleyebilir
        if (!note.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (request.getNoteText() != null) {
            note.setNoteText(request.getNoteText());
        }
        if (request.getTimestampSeconds() != null) {
            note.setTimestampSeconds(request.getTimestampSeconds());
        }
        note.setUpdatedAt(Instant.now());

        VideoNote updated = videoNoteRepository.save(note);

        return ResponseEntity.ok(VideoNoteResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteVideoNote(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VideoNote note = videoNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video note not found"));

        // Sadece kendi notunu silebilir
        if (!note.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        videoNoteRepository.delete(note);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Video note deleted successfully");
        return ResponseEntity.ok(response);
    }
}

