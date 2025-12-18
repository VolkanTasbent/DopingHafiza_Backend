package com.example.backend.controller;

import com.example.backend.dto.CreateKonuRequest;
import com.example.backend.dto.KonuDTO;
import com.example.backend.dto.KonuUpdateDTO;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Konu;
import com.example.backend.repository.KonuRepository;
import com.example.backend.service.KonuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/konu", "/api/konular"}) // Hem tekil hem çoğul destekle
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class KonuController {

    private final KonuService service;
    private final KonuRepository konuRepo;

    public KonuController(KonuService service, KonuRepository konuRepo) {
        this.service = service;
        this.konuRepo = konuRepo;
    }

    /** Belirli derse ait konuları listeler */
    @GetMapping
    public List<KonuDTO> list(@RequestParam Long dersId) {
        return service.listByDers(dersId).stream()
                .map(k -> service.toDTO(k))
                .toList();
    }

    /** Yeni konu oluşturur (ADMIN) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public KonuDTO create(@Valid @RequestBody CreateKonuRequest req) {
        // ✅ Tip güvenli! Spring otomatik validation yapar
        // ✅ dersId null veya negatif ise → 400 Bad Request
        // ✅ ad boş ise → 400 Bad Request
        Konu k = service.create(req.dersId(), req.ad());
        return service.toDTO(k);
    }

    /** Konu bilgilerini günceller (ADMIN) */
    @PutMapping("/{konuId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KonuDTO> updateKonu(
            @PathVariable Long konuId,
            @RequestBody KonuUpdateDTO updateDTO) {
        
        System.out.println("📥 Update request alındı - Konu ID: " + konuId);
        System.out.println("📥 Request body - Video URL: " + updateDTO.getKonuAnlatimVideosuUrl());
        System.out.println("📥 Request body - Ad: " + updateDTO.getAd());
        System.out.println("📥 Request body - Açıklama: " + updateDTO.getAciklama());
        System.out.println("📥 Request body - Döküman URL: " + updateDTO.getDokumanUrl());
        
        try {
            Konu updatedKonu = service.updateKonu(konuId, updateDTO);
            
            // Ders ID'sini güvenli şekilde al
            Long dersId = null;
            try {
                dersId = updatedKonu.getDers() != null ? updatedKonu.getDers().getId() : null;
            } catch (Exception e) {
                System.err.println("⚠️ Ders bilgisi alınamadı, konu ID'den ders bulunuyor: " + e.getMessage());
                // Alternatif: Konu'yu tekrar yükle
                Konu konuWithDers = konuRepo.findWithDersById(konuId)
                    .orElseThrow(() -> new ResourceNotFoundException("Konu bulunamadı: " + konuId));
                dersId = konuWithDers.getDers() != null ? konuWithDers.getDers().getId() : null;
            }
            
            KonuDTO dto = service.toDTO(updatedKonu);
            
            System.out.println("📤 Response - Konu ID: " + dto.id() + ", Video URL: " + dto.konuAnlatimVideosuUrl());
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            System.err.println("❌ Konu güncelleme hatası: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /** Konuyu siler (ADMIN) */
    @DeleteMapping("/{konuId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteKonu(@PathVariable Long konuId) {
        service.deleteKonu(konuId);
        return ResponseEntity.ok(Map.of("message", "Konu başarıyla silindi"));
    }

    /**
     * Sadece video URL güncelleme endpoint'i (Alternatif - Debug için)
     * Frontend'den sadece video URL gönderildiğinde kullanılabilir
     */
    @PutMapping("/{konuId}/video-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KonuDTO> updateVideoUrl(
            @PathVariable Long konuId,
            @RequestBody Map<String, String> request) {
        
        System.out.println("📥 Video URL update request - Konu ID: " + konuId);
        System.out.println("📥 Request body: " + request);
        
        String videoUrl = request.get("konuAnlatimVideosuUrl");
        System.out.println("📥 Video URL: " + videoUrl);
        
        // KonuUpdateDTO oluştur
        KonuUpdateDTO updateDTO = new KonuUpdateDTO();
        updateDTO.setKonuAnlatimVideosuUrl(videoUrl);
        
        try {
            Konu updatedKonu = service.updateKonu(konuId, updateDTO);
            
            // Ders ID'sini güvenli şekilde al
            Long dersId = null;
            try {
                dersId = updatedKonu.getDers() != null ? updatedKonu.getDers().getId() : null;
            } catch (Exception e) {
                System.err.println("⚠️ Ders bilgisi alınamadı, konu ID'den ders bulunuyor: " + e.getMessage());
                Konu konuWithDers = konuRepo.findWithDersById(konuId)
                    .orElseThrow(() -> new ResourceNotFoundException("Konu bulunamadı: " + konuId));
                dersId = konuWithDers.getDers() != null ? konuWithDers.getDers().getId() : null;
            }
            
            KonuDTO dto = service.toDTO(updatedKonu);
            
            System.out.println("📤 Response - Video URL: " + dto.konuAnlatimVideosuUrl());
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            System.err.println("❌ Video URL güncelleme hatası: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Konu videosunu sil (ADMIN)
     */
    @DeleteMapping("/video/{videoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteVideo(@PathVariable Long videoId) {
        service.deleteVideo(videoId);
        return ResponseEntity.ok(Map.of("message", "Video başarıyla silindi"));
    }
}
