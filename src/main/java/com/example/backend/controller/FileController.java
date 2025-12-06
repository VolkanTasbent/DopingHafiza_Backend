package com.example.backend.controller;

import com.example.backend.dto.AuthUserDTO;
import com.example.backend.dto.DenemeSinaviSoruDTO;
import com.example.backend.model.AppUser;
import com.example.backend.model.Konu;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.DenemeSinaviSoruRepository;
import com.example.backend.repository.KonuRepository;
import com.example.backend.service.DenemeSinaviService;
import com.example.backend.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class FileController {
    private final FileStorageService storage;
    private final AppUserRepository userRepo;
    private final KonuRepository konuRepo;
    private final DenemeSinaviSoruRepository denemeSoruRepo;
    private final DenemeSinaviService denemeSinaviService;

    public FileController(FileStorageService storage, AppUserRepository userRepo, KonuRepository konuRepo,
                          DenemeSinaviSoruRepository denemeSoruRepo, DenemeSinaviService denemeSinaviService) { 
        this.storage = storage;
        this.userRepo = userRepo;
        this.konuRepo = konuRepo;
        this.denemeSoruRepo = denemeSoruRepo;
        this.denemeSinaviService = denemeSinaviService;
    }

    /**
     * Genel dosya yükleme (sadece ADMIN)
     * Deneme sınavı soruları için çözüm videosu yükleme desteği
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> upload(@RequestPart("file") MultipartFile file,
                                      @RequestParam(required = false) Long soruId) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📹 POST /api/files/upload - Dosya yükleme");
        System.out.println("📹 Dosya adı: " + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "null"));
        System.out.println("📹 Dosya boyutu: " + file.getSize() + " bytes");
        System.out.println("📹 Content type: " + file.getContentType());
        System.out.println("📹 soruId: " + soruId);
        System.out.println("=".repeat(80) + "\n");
        
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Dosya boş");
            }
            
            // Eğer soruId varsa ve deneme sınavı sorusu ise, video yükleme endpoint'ini kullan
            if (soruId != null) {
                System.out.println("🔍 soruId var, deneme sınavı sorusu kontrolü yapılıyor...");
                boolean denemeSoruVarMi = denemeSoruRepo.existsById(soruId);
                System.out.println("🔍 Deneme sınavı sorusu var mı: " + denemeSoruVarMi);
                
                if (denemeSoruVarMi) {
                    System.out.println("✅ Deneme sınavı sorusu için video yükleme başlıyor...");
                    
                    // Dosya tipi kontrolü (video dosyaları)
                    String contentType = file.getContentType();
                    System.out.println("🔍 Content type kontrolü: " + contentType);
                    
                    if (contentType == null || !contentType.startsWith("video/")) {
                        System.out.println("❌ Hata: Video dosyası değil! Content type: " + contentType);
                        throw new IllegalArgumentException("Sadece video dosyaları yüklenebilir (gönderilen: " + contentType + ")");
                    }
                    
                    // Dosya boyutu kontrolü (max 100MB)
                    if (file.getSize() > 100 * 1024 * 1024) {
                        System.out.println("❌ Hata: Dosya çok büyük! Boyut: " + (file.getSize() / 1024 / 1024) + " MB");
                        throw new IllegalArgumentException("Video dosyası boyutu 100MB'dan küçük olmalıdır");
                    }
                    
                    // Video dosyasını kaydet
                    System.out.println("💾 Video dosyası kaydediliyor...");
                    String finalUrl;
                    try {
                        finalUrl = storage.saveVideo(file);
                        System.out.println("✅ Video kaydedildi: " + finalUrl);
                    } catch (IOException e) {
                        System.err.println("❌ Video dosyası kaydetme hatası: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("Video dosyası kaydedilemedi: " + e.getMessage(), e);
                    }
                    
                    // Deneme sınavı sorusunu güncelle
                    System.out.println("💾 Deneme sınavı sorusu güncelleniyor...");
                    DenemeSinaviSoruDTO updated;
                    try {
                        updated = denemeSinaviService.updateSoruCozumVideosu(soruId, finalUrl);
                        System.out.println("✅ Deneme sınavı sorusu güncellendi!");
                    } catch (Exception e) {
                        System.err.println("❌ Deneme sınavı sorusu güncelleme hatası: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("Deneme sınavı sorusu güncellenemedi: " + e.getMessage(), e);
                    }
                    
                    return Map.of(
                        "success", true,
                        "url", finalUrl,
                        "soruId", soruId,
                        "message", "Çözüm videosu başarıyla yüklendi",
                        "soru", updated
                    );
                } else {
                    System.out.println("⚠️ soruId var ama deneme sınavı sorusu değil, normal dosya yükleme yapılıyor...");
                }
            }
            
            // Normal dosya yükleme (soruId yok veya deneme sınavı sorusu değil)
            System.out.println("📁 Normal dosya yükleme yapılıyor...");
            String url;
            try {
                url = storage.save(file, "general");
                System.out.println("✅ Dosya kaydedildi: " + url);
            } catch (IOException e) {
                System.err.println("❌ Dosya kaydetme hatası: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Dosya kaydedilemedi: " + e.getMessage(), e);
            }
            return Map.of("url", url);
            
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Validation hatası: " + e.getMessage());
            throw e; // Re-throw validation errors
        } catch (RuntimeException e) {
            System.err.println("❌ Runtime hatası: " + e.getMessage());
            throw e; // Re-throw runtime errors
        } catch (Exception e) {
            System.err.println("❌ Beklenmeyen hata: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Dosya yükleme hatası: " + e.getMessage(), e);
        }
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
            user.getAvatarUrl(),
            user.getHedefSiralama(),
            user.getHedefUniversite(),
            user.getHedefBolum(),
            user.getHedefPuan(),
            user.getDarkMode() != null ? user.getDarkMode() : false
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

    /**
     * Konu anlatım videosu yükleme (sadece ADMIN)
     * Video dosyası yükleme veya YouTube/Vimeo URL'i kaydetme
     */
    @PostMapping(value = "/upload-konu-videosu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> uploadKonuVideosu(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam Long konuId,
            @RequestParam(required = false) String videoUrl
    ) throws Exception {
        
        Konu konu = konuRepo.findById(konuId)
                .orElseThrow(() -> new IllegalArgumentException("Konu bulunamadı: " + konuId));
        
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
            finalUrl = storage.saveVideo(file);
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
        
        // Konu'yu güncelle
        konu.setKonuAnlatimVideosuUrl(finalUrl);
        konuRepo.save(konu);
        
        return Map.of(
            "success", true,
            "url", finalUrl,
            "konuId", konuId,
            "message", "Konu anlatım videosu başarıyla yüklendi"
        );
    }

    /**
     * Konu dokümanını sil (sadece ADMIN)
     */
    @DeleteMapping("/dokuman/{konuId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> deleteDokuman(@PathVariable Long konuId) {
        Konu konu = konuRepo.findById(konuId)
                .orElseThrow(() -> new IllegalArgumentException("Konu bulunamadı: " + konuId));
        
        // Doküman bilgilerini temizle
        konu.setDokumanUrl(null);
        konu.setDokumanAdi(null);
        konuRepo.save(konu);
        
        return Map.of(
            "success", true,
            "konuId", konuId,
            "message", "Doküman başarıyla silindi"
        );
    }

    /**
     * Konu anlatım videosunu sil (sadece ADMIN)
     */
    @DeleteMapping("/konu-videosu/{konuId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> deleteKonuVideosu(@PathVariable Long konuId) {
        Konu konu = konuRepo.findById(konuId)
                .orElseThrow(() -> new IllegalArgumentException("Konu bulunamadı: " + konuId));
        
        // Video URL'ini temizle
        konu.setKonuAnlatimVideosuUrl(null);
        konuRepo.save(konu);
        
        return Map.of(
            "success", true,
            "konuId", konuId,
            "message", "Konu anlatım videosu başarıyla silindi"
        );
    }
}
