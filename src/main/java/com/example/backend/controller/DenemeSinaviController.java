package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.DenemeSinaviService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deneme-sinavlari")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class DenemeSinaviController {
    private final DenemeSinaviService service;

    public DenemeSinaviController(DenemeSinaviService service) {
        this.service = service;
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

    /** Deneme sınavı sorularını getir */
    @GetMapping("/{id}/sorular")
    public List<DenemeSinaviSoruDTO> getSorular(@PathVariable Long id) {
        return service.getSorular(id);
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

    /** Deneme sınavı sorularını quiz için getir (çözme) */
    @GetMapping("/{id}/quiz-sorular")
    public List<com.example.backend.dto.DenemeSinaviSoruDTOForQuiz> getSorularForQuiz(@PathVariable Long id) {
        return service.getSorularForQuiz(id);
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

