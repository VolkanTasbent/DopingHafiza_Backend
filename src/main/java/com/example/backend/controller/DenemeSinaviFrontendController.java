package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.service.DenemeSinaviService;
import com.example.backend.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Frontend için deneme sınavı endpoint'leri
 * Frontend'in beklediği format: { adi, kategori } yerine { ad, tip }
 */
@RestController
@RequestMapping("/api/deneme-sinavi")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class DenemeSinaviFrontendController {
    private final DenemeSinaviService service;
    private final QuizService quizService;
    private final AppUserRepository userRepo;

    public DenemeSinaviFrontendController(DenemeSinaviService service, QuizService quizService, AppUserRepository userRepo) {
        this.service = service;
        this.quizService = quizService;
        this.userRepo = userRepo;
    }

    /** Deneme sınavı oluştur (ADMIN) - Frontend format (adi, kategori) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DenemeSinaviResponseDTO createDenemeSinavi(@Valid @RequestBody CreateDenemeSinaviRequestFrontend request) {
        CreateDenemeSinaviRequest backendReq = request.toBackendRequest();
        DenemeSinaviDTO dto = service.create(backendReq);
        return DenemeSinaviResponseDTO.from(dto);
    }

    /** Tüm deneme sınavlarını listele - Frontend format */
    @GetMapping
    public List<DenemeSinaviResponseDTO> getAllDenemeSinavlari() {
        return service.listAll().stream()
                .map(DenemeSinaviResponseDTO::from)
                .toList();
    }

    /** Deneme sınavı detayı - Frontend format */
    @GetMapping("/{id}")
    public DenemeSinaviResponseDTO getDenemeSinaviById(@PathVariable Long id) {
        DenemeSinaviDTO dto = service.getById(id);
        return DenemeSinaviResponseDTO.from(dto);
    }

    /** Deneme sınavı submit - Frontend format */
    @PostMapping("/{id}/submit")
    public SubmitResponseDTO submitDenemeSinavi(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        return submitDenemeSinaviInternal(id, body, principal);
    }

    /** Deneme sınavı cevap gönder - Alternatif endpoint */
    @PostMapping("/{id}/cevap")
    public SubmitResponseDTO submitCevap(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        return submitDenemeSinaviInternal(id, body, principal);
    }

    /** Deneme sınavı cevaplar gönder - Alternatif endpoint */
    @PostMapping("/{id}/cevaplar")
    public SubmitResponseDTO submitCevaplar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        return submitDenemeSinaviInternal(id, body, principal);
    }

    /** Internal helper method */
    private SubmitResponseDTO submitDenemeSinaviInternal(
            Long id,
            Map<String, Object> body,
            Principal principal) {
        try {
            // DEBUG: Request body'yi logla
            System.out.println("\n" + "=".repeat(80));
            System.out.println("📥 POST /api/deneme-sinavi/" + id + "/submit - REQUEST BODY");
            System.out.println("📥 Request keys: " + body.keySet());
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String jsonBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
                System.out.println("📥 Full JSON body:");
                System.out.println(jsonBody);
            } catch (Exception e) {
                System.out.println("📥 Full body (toString): " + body);
            }
            System.out.println("=".repeat(80) + "\n");
            
            AppUser user = null;
            if (principal != null && principal.getName() != null) {
                user = userRepo.findByEmail(principal.getName()).orElse(null);
            }
            if (user == null) {
                System.err.println("⚠️ Kullanıcı bulunamadı! Principal: " + (principal != null ? principal.getName() : "null"));
            } else {
                System.out.println("✅ Kullanıcı bulundu: " + user.getEmail() + " (ID: " + user.getId() + ")");
            }
            
            // Request'ten gerekli alanları çıkar
            java.time.Instant startedAt = getInstant(body, "startedAt");
            java.time.Instant finishedAt = getInstant(body, "finishedAt");
            
            // Items'ı çıkar - CevapController'daki gibi gelişmiş parsing
            List<DenemeSinaviSubmitItemDTO> items = null;
            Object itemsObj = body.get("items");
            if (itemsObj == null) {
                itemsObj = body.get("answers");
            }
            if (itemsObj == null) {
                itemsObj = body.get("cevaplar");
            }
            
            if (itemsObj != null && itemsObj instanceof List<?> itemsList) {
                System.out.println("📦 Items listesi alındı, boyut: " + itemsList.size());
                if (!itemsList.isEmpty()) {
                    System.out.println("📦 İlk item örneği: " + itemsList.get(0));
                }
                
                items = itemsList.stream()
                        .map(item -> {
                            if (item instanceof Map<?, ?> itemMap) {
                                Map<String, Object> itemMapStr = (Map<String, Object>) itemMap;
                                System.out.println("  🔍 Item keys: " + itemMapStr.keySet());
                                
                                // SoruNo'yu bul (farklı field adlarını kontrol et)
                                Integer soruNo = getInteger(itemMapStr, "soruNo");
                                if (soruNo == null) soruNo = getInteger(itemMapStr, "soru_no");
                                if (soruNo == null) soruNo = getInteger(itemMapStr, "questionNumber");
                                if (soruNo == null) soruNo = getInteger(itemMapStr, "questionNo");
                                
                                // Eğer soruNo bulunamadıysa, soruId'den bul
                                if (soruNo == null) {
                                    Long soruId = getLong(itemMapStr, "soruId");
                                    if (soruId == null) soruId = getLong(itemMapStr, "soru_id");
                                    if (soruId == null) soruId = getLong(itemMapStr, "questionId");
                                    if (soruId == null) soruId = getLong(itemMapStr, "id");
                                    
                                    if (soruId != null) {
                                        // Soru ID'sinden soru numarasını bul
                                        DenemeSinaviSoruDTO soru = service.getSoruById(soruId);
                                        if (soru != null) {
                                            soruNo = soru.soruNo();
                                            System.out.println("  🔄 SoruId " + soruId + " -> SoruNo " + soruNo);
                                        }
                                    }
                                }
                                
                                // Cevabı bul (farklı field adlarını kontrol et)
                                String secilenCevap = getString(itemMapStr, "secilenCevap");
                                if (secilenCevap == null) secilenCevap = getString(itemMapStr, "secilen_cevap");
                                if (secilenCevap == null) secilenCevap = getString(itemMapStr, "answer");
                                if (secilenCevap == null) secilenCevap = getString(itemMapStr, "cevap");
                                if (secilenCevap == null) secilenCevap = getString(itemMapStr, "selectedAnswer");
                                if (secilenCevap == null) secilenCevap = getString(itemMapStr, "selected");
                                
                                // Eğer cevap bulunamadıysa, secenekId'yi kullan (fake ID formatı)
                                if (secilenCevap == null) {
                                    Long secenekId = getLong(itemMapStr, "secenekId");
                                    if (secenekId == null) secenekId = getLong(itemMapStr, "sikId");
                                    if (secenekId == null) secenekId = getLong(itemMapStr, "secenek_id");
                                    
                                    if (secenekId != null && secenekId > 1000) {
                                        // Fake ID formatı: soruId * 1000 + sıralama
                                        Long soruIdFromFakeId = secenekId / 1000;
                                        Long siralamaFromFakeId = secenekId % 1000;
                                        
                                        if (siralamaFromFakeId >= 1 && siralamaFromFakeId <= 5) {
                                            secilenCevap = String.valueOf((char)('A' + siralamaFromFakeId.intValue() - 1));
                                            System.out.println("  🔄 SecenekId " + secenekId + " (fake ID, sıralama: " + siralamaFromFakeId + ") -> Harf: '" + secilenCevap + "'");
                                            
                                            // Eğer soruNo hala yoksa, fake ID'den soruId'yi kullan
                                            if (soruNo == null) {
                                                DenemeSinaviSoruDTO denemeSoru = service.getSoruById(soruIdFromFakeId);
                                                if (denemeSoru != null) {
                                                    soruNo = denemeSoru.soruNo();
                                                    System.out.println("  🔄 SoruId " + soruIdFromFakeId + " -> SoruNo " + soruNo);
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                System.out.println("  📝 Sonuç: SoruNo=" + soruNo + ", Seçilen='" + secilenCevap + "'");
                                if (soruNo != null) {
                                    return new DenemeSinaviSubmitItemDTO(soruNo, secilenCevap);
                                }
                            }
                            return null;
                        })
                        .filter(item -> item != null)
                        .toList();
                
                System.out.println("✅ Parse edilen items sayısı: " + (items != null ? items.size() : 0));
                if (items != null && !items.isEmpty()) {
                    System.out.println("📋 Parse edilen items (ilk 5):");
                    for (int i = 0; i < Math.min(items.size(), 5); i++) {
                        DenemeSinaviSubmitItemDTO item = items.get(i);
                        System.out.println("  [" + i + "] SoruNo: " + item.soruNo() + ", Seçilen: '" + item.secilenCevap() + "'");
                    }
                }
            } else {
                System.out.println("⚠️ Items null veya list değil: " + (itemsObj != null ? itemsObj.getClass().getName() : "null"));
            }
            
            DenemeSinaviSubmitRequest request = 
                new DenemeSinaviSubmitRequest(id, startedAt, finishedAt, items);
            
            System.out.println("🚀 Deneme sınavı submit başlatılıyor: ID=" + id + ", Items=" + (items != null ? items.size() : 0));
            if (items == null || items.isEmpty()) {
                System.out.println("⚠️⚠️⚠️ UYARI: Items NULL veya BOŞ! Frontend cevapları göndermiyor!");
            }
            System.out.println("=".repeat(80) + "\n");
            
            return quizService.submitDenemeSinavi(request, user);
        } catch (Exception e) {
            System.err.println("❌ Deneme sınavı submit hatası: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Deneme sınavı gönderilemedi: " + e.getMessage(), e);
        }
    }
    
    private java.time.Instant getInstant(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) return null;
        if (value instanceof String str) {
            try {
                return java.time.Instant.parse(str);
            } catch (Exception e) {
                return null;
            }
        }
        if (value instanceof Number num) {
            return java.time.Instant.ofEpochMilli(num.longValue());
        }
        return null;
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
    
    private String getString(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) return null;
        String str = value.toString().trim();
        return str.isEmpty() ? null : str;
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

