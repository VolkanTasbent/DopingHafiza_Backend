package com.example.backend.controller;

import com.example.backend.dto.AuthUserDTO;
import com.example.backend.model.AppUser;
import com.example.backend.model.Konu;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.KonuRepository;
import com.example.backend.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class FileController {
    private final FileStorageService storage;
    private final AppUserRepository userRepo;
    private final KonuRepository konuRepo;

    public FileController(FileStorageService storage, AppUserRepository userRepo, KonuRepository konuRepo) { 
        this.storage = storage;
        this.userRepo = userRepo;
        this.konuRepo = konuRepo;
    }

    /**
     * Genel dosya yükleme (sadece ADMIN)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,String> upload(@RequestPart("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("Dosya boş");
        String url = storage.save(file, "general");
        return Map.of("url", url);
    }

    /**
     * Profil resmi yükleme (tüm kullanıcılar kendi profilleri için)
     * Dosyayı kaydeder ve otomatik olarak kullanıcının profilini günceller
     */
    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public AuthUserDTO uploadAvatar(
            @RequestPart("file") MultipartFile file,
            Authentication auth) throws Exception {
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş");
        }
        
        // Dosya tipi kontrolü
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Sadece resim dosyaları yüklenebilir");
        }
        
        // Dosya boyutu kontrolü (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Dosya boyutu 5MB'dan küçük olmalıdır");
        }
        
        // Kullanıcı bilgilerini al
        String email = auth.getName();
        AppUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
        
        // Profil resmini kaydet
        String url = storage.saveAvatar(file, email);
        
        // Database'i güncelle
        user.setAvatarUrl(url);
        userRepo.save(user);
        
        // Güncel kullanıcı bilgilerini döndür
        return new AuthUserDTO(
            user.getId(), 
            user.getEmail(), 
            user.getAd(), 
            user.getSoyad(), 
            user.getRole(), 
            user.getAvatarUrl()
        );
    }

    /**
     * Konu dökümanı yükleme (sadece ADMIN)
     * Dosyayı kaydeder ve konuyu otomatik günceller
     */
    @PostMapping(value = "/upload-dokuman", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> uploadDokuman(
            @RequestPart("file") MultipartFile file,
            @RequestParam Long konuId,
            @RequestParam(required = false) String dokumanAdi
    ) throws Exception {
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş");
        }
        
        // Dosya tipi kontrolü (sadece PDF)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Sadece PDF dosyaları yüklenebilir");
        }
        
        // Dosya boyutu kontrolü (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Dosya boyutu 10MB'dan küçük olmalıdır");
        }
        
        // Konu'yu bul
        Konu konu = konuRepo.findById(konuId)
                .orElseThrow(() -> new IllegalArgumentException("Konu bulunamadı: " + konuId));
        
        // Dökümanı kaydet
        String url = storage.saveDokuman(file);
        
        // Konu'yu güncelle
        konu.setDokumanUrl(url);
        konu.setDokumanAdi(dokumanAdi != null ? dokumanAdi : file.getOriginalFilename());
        konuRepo.save(konu);
        
        return Map.of(
            "success", true,
            "url", url,
            "konuId", konuId,
            "dokumanAdi", konu.getDokumanAdi(),
            "message", "Döküman başarıyla yüklendi"
        );
    }
}
