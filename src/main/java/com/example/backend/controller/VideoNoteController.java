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
    public ResponseEntity<?> createVideoNote(
            @Valid @RequestBody CreateVideoNoteRequest request,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            AppUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            System.out.println("📥 Video note ekleme isteği:");
            System.out.println("  - Konu ID: " + request.getKonuId());
            System.out.println("  - Video ID: " + request.getVideoId());
            System.out.println("  - Video URL: " + request.getVideoUrl());
            System.out.println("  - Note Text: " + (request.getNoteText() != null ? request.getNoteText().substring(0, Math.min(50, request.getNoteText().length())) + "..." : "null"));
            System.out.println("  - Timestamp: " + request.getTimestampSeconds());
            System.out.println("  - User ID: " + user.getId());

            VideoNote note = new VideoNote();
            note.setUserId(user.getId());
            note.setKonuId(request.getKonuId());
            
            // videoId varsa ve videoUrl'den farklıysa kaydet
            // Aksi halde null bırak (geriye dönük uyumluluk için videoUrl kullanılacak)
            if (request.getVideoId() != null && !request.getVideoId().trim().isEmpty() 
                && !request.getVideoId().equals(request.getVideoUrl())) {
                note.setVideoId(request.getVideoId().trim());
                System.out.println("  - Video ID kaydediliyor: " + request.getVideoId());
            } else {
                note.setVideoId(null);
                System.out.println("  - Video ID kaydedilmiyor (videoUrl kullanılacak)");
            }
            
            note.setVideoUrl(request.getVideoUrl());
            note.setNoteText(request.getNoteText());
            note.setTimestampSeconds(request.getTimestampSeconds());

            VideoNote saved = videoNoteRepository.save(note);
            
            System.out.println("✅ Video note başarıyla kaydedildi:");
            System.out.println("  - Note ID: " + saved.getId());
            System.out.println("  - User ID: " + saved.getUserId());
            System.out.println("  - Konu ID: " + saved.getKonuId());
            System.out.println("  - Video ID: " + saved.getVideoId());
            System.out.println("  - Video URL: " + saved.getVideoUrl());
            System.out.println("  - Timestamp: " + saved.getTimestampSeconds());
            
            // Veritabanından tekrar okuyarak doğrula
            VideoNote verify = videoNoteRepository.findById(saved.getId()).orElse(null);
            if (verify != null) {
                System.out.println("✅ Veritabanı doğrulaması başarılı - Note ID: " + verify.getId() + ", User ID: " + verify.getUserId());
            } else {
                System.err.println("❌ UYARI: Not kaydedildi ama veritabanından okunamadı!");
            }

            return ResponseEntity.ok(VideoNoteResponse.from(saved));
        } catch (Exception e) {
            System.err.println("❌ Video note ekleme hatası: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Video note eklenemedi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VideoNotesResponse> getVideoNotes(
            @RequestParam(required = false) Long konuId,
            @RequestParam(required = false) String videoId, // YENİ: videoId parametresi
            @RequestParam(required = false) String videoUrl,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("📥 Video note listeleme isteği:");
        System.out.println("  - Konu ID: " + konuId);
        System.out.println("  - Video ID: " + videoId + " (null? " + (videoId == null) + ", empty? " + (videoId != null && videoId.trim().isEmpty()) + ")");
        System.out.println("  - Video URL: " + videoUrl + " (null? " + (videoUrl == null) + ", empty? " + (videoUrl != null && videoUrl.trim().isEmpty()) + ")");
        System.out.println("  - Video ID == Video URL? " + (videoId != null && videoUrl != null && videoId.equals(videoUrl)));
        System.out.println("  - User ID: " + user.getId());

        List<VideoNote> notes;
        
        if (konuId != null) {
            // videoId varsa ve videoUrl'den farklıysa, önce videoId ile ara
            if (videoId != null && !videoId.trim().isEmpty() 
                && (videoUrl == null || !videoId.equals(videoUrl))) {
                System.out.println("🔍 Filtreleme: konuId + videoId (videoId != videoUrl)");
                notes = videoNoteRepository.findByKonuIdAndVideoIdAndUserIdOrderByTimestampSecondsAsc(
                    konuId, videoId.trim(), user.getId()
                );
                System.out.println("🔍 VideoId ile bulunan not sayısı: " + notes.size());
                
                // Eğer videoId ile bulunamadıysa, videoUrl ile de dene
                // (eski notlar videoId olmadan kaydedilmiş olabilir)
                if (notes.isEmpty() && videoUrl != null && !videoUrl.trim().isEmpty()) {
                    System.out.println("🔍 VideoId ile bulunamadı, videoUrl ile deneniyor...");
                    List<VideoNote> urlNotes = videoNoteRepository.findByKonuIdAndVideoUrlAndUserIdOrderByTimestampSecondsAsc(
                        konuId, videoUrl.trim(), user.getId()
                    );
                    // videoId null olan veya eşleşen kayıtları ekle
                    notes = urlNotes.stream()
                        .filter(n -> n.getVideoId() == null || n.getVideoId().equals(videoId.trim()))
                        .collect(Collectors.toList());
                    System.out.println("🔍 VideoUrl ile bulunan not sayısı: " + notes.size());
                }
            }
            // videoId yoksa veya videoUrl ile aynıysa, videoUrl ile ara
            else if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                System.out.println("🔍 Filtreleme: konuId + videoUrl (videoId yok veya videoUrl ile aynı)");
                notes = videoNoteRepository.findByKonuIdAndVideoUrlAndUserIdOrderByTimestampSecondsAsc(
                    konuId, videoUrl.trim(), user.getId()
                );
                System.out.println("🔍 VideoUrl ile bulunan not sayısı: " + notes.size());
            }
            // Sadece konuId varsa, konuId'ye göre filtrele (tüm videolar)
            else {
                System.out.println("🔍 Filtreleme: sadece konuId (tüm videolar)");
                notes = videoNoteRepository.findByUserIdAndKonuIdOrderByTimestampSecondsAsc(user.getId(), konuId);
                System.out.println("🔍 KonuId ile bulunan not sayısı: " + notes.size());
            }
        }
        // Sadece videoUrl varsa (konuId yok), videoUrl'e göre filtrele
        else if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            System.out.println("🔍 Filtreleme: sadece videoUrl");
            notes = videoNoteRepository.findByUserIdAndVideoUrlOrderByTimestampSecondsAsc(user.getId(), videoUrl.trim());
            System.out.println("🔍 VideoUrl ile bulunan not sayısı: " + notes.size());
        }
        // Hiçbiri yoksa, tüm notları getir
        else {
            System.out.println("🔍 Filtreleme: tüm notlar");
            notes = videoNoteRepository.findByUserIdOrderByTimestampSecondsAsc(user.getId());
            System.out.println("🔍 Tüm notlar: " + notes.size());
        }

        System.out.println("📊 Bulunan not sayısı: " + notes.size());
        
        // Bulunan notların detaylarını logla
        if (notes.isEmpty()) {
            System.out.println("⚠️ UYARI: Hiç not bulunamadı!");
        } else {
            for (VideoNote note : notes) {
                System.out.println("  ✅ Note ID: " + note.getId() + 
                    ", User ID: " + note.getUserId() + 
                    ", Konu ID: " + note.getKonuId() + 
                    ", Video ID: " + (note.getVideoId() != null ? note.getVideoId() : "NULL") +
                    ", Video URL: " + (note.getVideoUrl() != null ? note.getVideoUrl().substring(0, Math.min(50, note.getVideoUrl().length())) : "NULL"));
            }
        }
        
        // Tüm notları kontrol et (debug için)
        List<VideoNote> allUserNotes = videoNoteRepository.findByUserId(user.getId());
        System.out.println("📊 Kullanıcının toplam not sayısı: " + allUserNotes.size());
        if (allUserNotes.isEmpty()) {
            System.out.println("⚠️ UYARI: Kullanıcının hiç notu yok!");
        } else {
            System.out.println("📋 Tüm notlar (filtreleme olmadan):");
            for (VideoNote note : allUserNotes) {
                boolean matches = false;
                if (konuId != null && note.getKonuId().equals(konuId)) {
                    if (videoId != null && !videoId.trim().isEmpty() && !videoId.equals(videoUrl)) {
                        matches = videoId.trim().equals(note.getVideoId());
                    } else if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                        matches = videoUrl.trim().equals(note.getVideoUrl());
                    } else {
                        matches = true; // Sadece konuId eşleşiyor
                    }
                }
                System.out.println("  " + (matches ? "✅" : "❌") + 
                    " Note ID: " + note.getId() + 
                    ", Konu ID: " + note.getKonuId() + 
                    ", Video ID: " + (note.getVideoId() != null ? note.getVideoId() : "NULL") + 
                    ", Video URL: " + (note.getVideoUrl() != null ? note.getVideoUrl().substring(0, Math.min(50, note.getVideoUrl().length())) : "NULL") +
                    ", Timestamp: " + note.getTimestampSeconds());
            }
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

    /**
     * Debug endpoint: Kullanıcının tüm notlarını getir (videoId'ye bakmadan)
     */
    @GetMapping("/debug/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAllVideoNotesDebug(Authentication authentication) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<VideoNote> allNotes = videoNoteRepository.findByUserId(user.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("totalNotes", allNotes.size());
        response.put("notes", allNotes.stream()
                .map(n -> Map.of(
                    "id", n.getId(),
                    "konuId", n.getKonuId(),
                    "videoId", n.getVideoId() != null ? n.getVideoId() : "null",
                    "videoUrl", n.getVideoUrl() != null ? n.getVideoUrl().substring(0, Math.min(50, n.getVideoUrl().length())) : "null",
                    "timestamp", n.getTimestampSeconds(),
                    "noteText", n.getNoteText() != null ? n.getNoteText().substring(0, Math.min(30, n.getNoteText().length())) : "null"
                ))
                .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
}






