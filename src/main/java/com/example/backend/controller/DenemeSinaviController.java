package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.DenemeSinaviService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.TimeUnit;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deneme-sinavlari")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class DenemeSinaviController {
    private final DenemeSinaviService service;
    private final com.example.backend.service.FileStorageService fileStorage;

    public DenemeSinaviController(DenemeSinaviService service, 
                                   com.example.backend.service.FileStorageService fileStorage) {
        this.service = service;
        this.fileStorage = fileStorage;
    }

    /** Tüm deneme sınavlarını listele - Backend format */
    @GetMapping
    public List<DenemeSinaviDTO> list(@RequestParam(required = false) String tip) {
        if (tip != null && (tip.equals("TYT") || tip.equals("AYT"))) {
            return service.listByTip(tip);
        }
        return service.listAll();
    }

    /** Deneme sınavı detayı - Backend format */
    @GetMapping("/{id}")
    public DenemeSinaviDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    /** Tüm deneme sınavı sorularını getir - SoruDTO formatında (Admin panel için) */
    @GetMapping("/sorular/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.example.backend.dto.SoruDTO>> getAllSorular() {
        List<com.example.backend.dto.SoruDTO> sorular = service.getAllSorularAsSoruDTO();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(sorular);
    }

    /** Deneme sınavı sorularını getir - SoruDTO formatında (seçenekler dahil) */
    @GetMapping("/{id}/sorular")
    public ResponseEntity<List<com.example.backend.dto.SoruDTO>> getSorular(@PathVariable Long id) {
        List<com.example.backend.dto.SoruDTO> sorular = service.getSorularAsSoruDTO(id);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(sorular);
    }

    /** Deneme sınavı oluştur (ADMIN) - Backend format */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DenemeSinaviDTO create(@Valid @RequestBody CreateDenemeSinaviRequest req) {
        return service.create(req);
    }

    /** Deneme sınavını güncelle (ADMIN) */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DenemeSinaviDTO update(@PathVariable Long id, @Valid @RequestBody CreateDenemeSinaviRequest req) {
        return service.update(id, req);
    }

    /** Deneme sınavını sil (ADMIN) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /** Soru ekle (ADMIN) */
    @PostMapping("/{denemeId}/sorular")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DenemeSinaviSoruDTO addSoru(
            @PathVariable Long denemeId,
            @RequestBody Map<String, Object> body) {
        String soruMetni = getString(body, "soruMetni", true);
        String sikA = getString(body, "sikA", false);
        String sikB = getString(body, "sikB", false);
        String sikC = getString(body, "sikC", false);
        String sikD = getString(body, "sikD", false);
        String sikE = getString(body, "sikE", false);
        String dogruCevap = getString(body, "dogruCevap", true);
        Integer zorluk = getInteger(body, "zorluk");
        String konular = getString(body, "konular", false);
        String aciklama = getString(body, "aciklama", false);
        Integer soruNo = getInteger(body, "soruNo");
        Long dersId = getLong(body, "dersId");

        return service.addSoru(denemeId, dersId, soruMetni, sikA, sikB, sikC, sikD, sikE,
                dogruCevap, zorluk, konular, aciklama, soruNo);
    }

    /** Soru güncelle (ADMIN) */
    @PutMapping("/sorular/{soruId}")
    @PreAuthorize("hasRole('ADMIN')")
    public DenemeSinaviSoruDTO updateSoru(@PathVariable Long soruId,
                                           @Valid @RequestBody UpdateDenemeSinaviSoruRequest req) {
        return service.updateSoru(soruId, req);
    }

    /** Soru sil (ADMIN) */
    @DeleteMapping("/sorular/{soruId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSoru(@PathVariable Long soruId) {
        service.deleteSoru(soruId);
    }

    /** CSV'den toplu soru yükleme (ADMIN) */
    @PostMapping(value = "/{denemeId}/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> importCSV(@PathVariable Long denemeId,
                                         @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("text/csv") && !contentType.equals("text/plain") 
                && !file.getOriginalFilename().endsWith(".csv"))) {
            throw new IllegalArgumentException("Sadece CSV dosyaları yüklenebilir");
        }

        return service.importFromCSV(denemeId, file);
    }

    /** Deneme sınavı sorularını quiz için getir (çözme) - SoruDTO formatında */
    @GetMapping("/{id}/quiz-sorular")
    public ResponseEntity<List<com.example.backend.dto.SoruDTO>> getSorularForQuiz(@PathVariable Long id) {
        List<com.example.backend.dto.SoruDTO> sorular = service.getSorularForQuizAsSoruDTO(id);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(sorular);
    }

    /** Tek deneme sınavı sorusu getir (ID ile) */
    @GetMapping("/sorular/{soruId}")
    public DenemeSinaviSoruDTO getSoruById(@PathVariable Long soruId) {
        return service.getSoruById(soruId);
    }

    /** Deneme sınavı sorusu çözüm videosu yükle (ADMIN) */
    @PostMapping(value = "/sorular/{soruId}/cozum-videosu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> uploadCozumVideosu(
            @PathVariable Long soruId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(required = false) String videoUrl) throws Exception {
        
        String finalUrl = null;
        
        // Öncelik: Dosya yüklenmişse dosyayı kaydet
        if (file != null && !file.isEmpty()) {
            // Dosya tipi kontrolü (video dosyaları)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new IllegalArgumentException("Sadece video dosyaları yüklenebilir");
            }
            
            // Dosya boyutu kontrolü (max 100MB)
            if (file.getSize() > 100 * 1024 * 1024) {
                throw new IllegalArgumentException("Video dosyası boyutu 100MB'dan küçük olmalıdır");
            }
            
            // Video dosyasını kaydet
            finalUrl = fileStorage.saveVideo(file);
        } 
        // Eğer dosya yoksa, URL ile kaydet (YouTube, Vimeo vb.)
        else if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            String trimmed = videoUrl.trim();
            // URL validasyonu (basit kontrol)
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                throw new IllegalArgumentException("Geçerli bir URL giriniz (http:// veya https:// ile başlamalı)");
            }
            if (trimmed.length() > 500) {
                throw new IllegalArgumentException("Video URL maksimum 500 karakter olabilir");
            }
            finalUrl = trimmed;
        } else {
            throw new IllegalArgumentException("Video dosyası veya URL gerekli");
        }
        
        // Soruyu güncelle
        DenemeSinaviSoruDTO updated = service.updateSoruCozumVideosu(soruId, finalUrl);
        
        return Map.of(
            "success", true,
            "url", finalUrl,
            "soruId", soruId,
            "message", "Çözüm videosu başarıyla yüklendi",
            "soru", updated
        );
    }


    // ---- Helper methods ----
    private String getString(Map<String, Object> body, String key, boolean required) {
        Object value = body.get(key);
        if (value == null) {
            if (required) {
                throw new IllegalArgumentException(key + " eksik");
            }
            return null;
        }
        return value.toString().trim();
    }

    private Integer getInteger(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long getLong(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

