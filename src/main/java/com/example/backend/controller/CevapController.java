package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.model.AppUser;
import com.example.backend.model.DenemeSinavi;
import com.example.backend.model.DenemeSinaviSoru;
import com.example.backend.model.Secenek;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.DenemeSinaviRepository;
import com.example.backend.repository.DenemeSinaviSoruRepository;
import com.example.backend.repository.SecenekRepository;
import com.example.backend.service.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cevap")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class CevapController {
    private final QuizService quizService;
    private final AppUserRepository userRepo;
    private final DenemeSinaviSoruRepository denemeSoruRepo;
    private final DenemeSinaviRepository denemeRepo;
    private final SecenekRepository secenekRepo;

    public CevapController(QuizService quizService, AppUserRepository userRepo, 
                          DenemeSinaviSoruRepository denemeSoruRepo, DenemeSinaviRepository denemeRepo,
                          SecenekRepository secenekRepo) {
        this.quizService = quizService;
        this.userRepo = userRepo;
        this.denemeSoruRepo = denemeSoruRepo;
        this.denemeRepo = denemeRepo;
        this.secenekRepo = secenekRepo;
    }

    /** Deneme sınavı sonuçlarını toplu gönder - Frontend'in beklediği format */
    @PostMapping("/bulk")
    public SubmitResponseDTO submitBulk(@RequestBody Map<String, Object> body, Principal principal) {
        // DEBUG: Request body'yi detaylı logla
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📥 ===== RAW REQUEST BODY RECEIVED =====");
        System.out.println("📥 Request keys: " + body.keySet());
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            System.out.println("📥 Full JSON body:");
            System.out.println(jsonBody);
        } catch (Exception e) {
            System.out.println("📥 Full body (toString): " + body);
            System.out.println("📥 Error serializing: " + e.getMessage());
        }
        System.out.println("=".repeat(80) + "\n");
        
        AppUser user = null;
        if (principal != null && principal.getName() != null) {
            user = userRepo.findByEmail(principal.getName()).orElse(null);
        }

        // Request'ten gerekli alanları çıkar
        Long denemeSinaviId = getLong(body, "denemeSinaviId");
        if (denemeSinaviId == null) {
            throw new IllegalArgumentException("denemeSinaviId gerekli");
        }

        java.time.Instant startedAt = getInstant(body, "startedAt");
        java.time.Instant finishedAt = getInstant(body, "finishedAt");

        // Items'ı çıkar ve DenemeSinaviSubmitItemDTO listesine çevir
        // Frontend farklı key'ler kullanıyor olabilir, hepsini kontrol et
        List<DenemeSinaviSubmitItemDTO> items = null;
        Object itemsObj = body.get("items");
        if (itemsObj == null) {
            itemsObj = body.get("answers"); // Alternatif key
        }
        if (itemsObj == null) {
            itemsObj = body.get("cevaplar"); // Alternatif key
        }
        
        if (itemsObj != null && itemsObj instanceof List<?> itemsList) {
            System.out.println("📦 Items listesi alındı, boyut: " + itemsList.size());
            System.out.println("📦 İlk item örneği: " + (itemsList.isEmpty() ? "boş" : itemsList.get(0)));
            
            items = itemsList.stream()
                    .map(item -> {
                        if (item instanceof Map<?, ?> itemMap) {
                            Map<String, Object> itemMapStr = (Map<String, Object>) itemMap;
                            System.out.println("  🔍 Item keys: " + itemMapStr.keySet());
                            
                            // Önce soruNo'yu bul (direkt soru numarası)
                            Integer soruNo = getInteger(itemMapStr, "soruNo");
                            if (soruNo == null) soruNo = getInteger(itemMapStr, "soru_no");
                            if (soruNo == null) soruNo = getInteger(itemMapStr, "questionNumber");
                            if (soruNo == null) soruNo = getInteger(itemMapStr, "questionNo");
                            
                            // Eğer soruNo bulunamadıysa, soruId'yi soruNo'ya çevir
                            if (soruNo == null) {
                                Long soruId = getLong(itemMapStr, "soruId");
                                if (soruId == null) soruId = getLong(itemMapStr, "soru_id");
                                if (soruId == null) soruId = getLong(itemMapStr, "questionId");
                                if (soruId == null) soruId = getLong(itemMapStr, "id");
                                
                                if (soruId != null) {
                                    // Soru ID'sinden soru numarasını bul
                                    DenemeSinaviSoru soru = denemeSoruRepo.findById(soruId).orElse(null);
                                    if (soru != null) {
                                        soruNo = soru.getSoruNo();
                                        System.out.println("  🔄 SoruId " + soruId + " -> SoruNo " + soruNo);
                                    } else {
                                        System.out.println("  ⚠️ SoruId " + soruId + " bulunamadı!");
                                    }
                                }
                            }
                            
                            // Cevabı bul
                            String secilenCevap = getString(itemMapStr, "secilenCevap");
                            if (secilenCevap == null) secilenCevap = getString(itemMapStr, "secilen_cevap");
                            if (secilenCevap == null) secilenCevap = getString(itemMapStr, "answer");
                            if (secilenCevap == null) secilenCevap = getString(itemMapStr, "cevap");
                            if (secilenCevap == null) secilenCevap = getString(itemMapStr, "selectedAnswer");
                            if (secilenCevap == null) secilenCevap = getString(itemMapStr, "selected");
                            
                            // Eğer hala soruNo bulunamadıysa ve soruId varsa, deneme sınavı sorusunu kontrol et
                            if (soruNo == null) {
                                Long soruIdForDeneme = getLong(itemMapStr, "soruId");
                                if (soruIdForDeneme == null) soruIdForDeneme = getLong(itemMapStr, "soru_id");
                                if (soruIdForDeneme == null) soruIdForDeneme = getLong(itemMapStr, "questionId");
                                if (soruIdForDeneme == null) soruIdForDeneme = getLong(itemMapStr, "id");
                                
                                if (soruIdForDeneme != null) {
                                    // Bu ID deneme sınavı sorusu mu kontrol et
                                    DenemeSinaviSoru denemeSoru = denemeSoruRepo.findById(soruIdForDeneme).orElse(null);
                                    if (denemeSoru != null && denemeSinaviId != null && 
                                        denemeSoru.getDenemeSinavi() != null && 
                                        denemeSoru.getDenemeSinavi().getId().equals(denemeSinaviId)) {
                                        soruNo = denemeSoru.getSoruNo();
                                        System.out.println("  🔄 SoruId " + soruIdForDeneme + " (deneme sınavı sorusu) -> SoruNo " + soruNo);
                                    }
                                }
                            }
                            
                            // Eğer cevap bulunamadıysa, secenekId'yi kullan
                            if (secilenCevap == null) {
                                Long secenekId = getLong(itemMapStr, "secenekId");
                                if (secenekId == null) secenekId = getLong(itemMapStr, "sikId");
                                if (secenekId == null) secenekId = getLong(itemMapStr, "secenek_id");
                                
                                if (secenekId != null) {
                                    // Önce normal secenek tablosunda ara
                                    Secenek secenek = secenekRepo.findById(secenekId).orElse(null);
                                    if (secenek != null && secenek.getSiralama() != null) {
                                        // Normal sorular için: Sıralama: 1=A, 2=B, 3=C, 4=D, 5=E
                                        int siralama = secenek.getSiralama();
                                        if (siralama >= 1 && siralama <= 5) {
                                            secilenCevap = String.valueOf((char)('A' + siralama - 1));
                                            System.out.println("  🔄 SecenekId " + secenekId + " (normal soru, sıralama: " + siralama + ") -> Harf: '" + secilenCevap + "'");
                                        }
                                    } else {
                                        // Deneme sınavı soruları için: Fake ID formatı = soruId * 1000 + sıralama
                                        // Örnek: 109001 = Soru 109, Sıralama 1 (A)
                                        if (soruNo != null && denemeSinaviId != null) {
                                            // SoruNo'dan soruId'yi bul
                                            DenemeSinavi deneme = denemeRepo.findById(denemeSinaviId).orElse(null);
                                            DenemeSinaviSoru denemeSoru = null;
                                            if (deneme != null) {
                                                denemeSoru = denemeSoruRepo.findByDenemeSinaviAndSoruNo(deneme, soruNo).orElse(null);
                                            }
                                            
                                            if (denemeSoru != null) {
                                                Long soruIdFromDeneme = denemeSoru.getId();
                                                // Fake ID'den sıralamayı çıkar
                                                Long siralamaFromFakeId = secenekId % 1000;
                                                if (siralamaFromFakeId >= 1 && siralamaFromFakeId <= 5) {
                                                    secilenCevap = String.valueOf((char)('A' + siralamaFromFakeId.intValue() - 1));
                                                    System.out.println("  🔄 SecenekId " + secenekId + " (deneme sınavı, fake ID, sıralama: " + siralamaFromFakeId + ") -> Harf: '" + secilenCevap + "'");
                                                }
                                            }
                                        } else {
                                            // Fake ID formatını parse et: soruId * 1000 + sıralama
                                            Long soruIdFromFakeId = secenekId / 1000;
                                            Long siralamaFromFakeId = secenekId % 1000;
                                            
                                            if (soruIdFromFakeId > 0 && siralamaFromFakeId >= 1 && siralamaFromFakeId <= 5) {
                                                // SoruId'den soruNo'yu bul
                                                DenemeSinaviSoru denemeSoru = denemeSoruRepo.findById(soruIdFromFakeId).orElse(null);
                                                if (denemeSoru != null && denemeSinaviId != null && 
                                                    denemeSoru.getDenemeSinavi() != null && 
                                                    denemeSoru.getDenemeSinavi().getId().equals(denemeSinaviId)) {
                                                    soruNo = denemeSoru.getSoruNo();
                                                    secilenCevap = String.valueOf((char)('A' + siralamaFromFakeId.intValue() - 1));
                                                    System.out.println("  🔄 SecenekId " + secenekId + " (fake ID, soruId: " + soruIdFromFakeId + ", sıralama: " + siralamaFromFakeId + ") -> SoruNo: " + soruNo + ", Harf: '" + secilenCevap + "'");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            System.out.println("  📝 SoruNo: " + soruNo + ", Seçilen: '" + secilenCevap + "'");
                            if (soruNo != null) {
                                return new DenemeSinaviSubmitItemDTO(soruNo, secilenCevap);
                            }
                        }
                        return null;
                    })
                    .filter(item -> item != null)
                    .toList();
            System.out.println("✅ Parse edilen items sayısı: " + (items != null ? items.size() : 0));
        } else {
            System.out.println("⚠️ Items null veya list değil: " + (itemsObj != null ? itemsObj.getClass().getName() : "null"));
        }

        // DenemeSinaviSubmitRequest oluştur
        DenemeSinaviSubmitRequest request = new DenemeSinaviSubmitRequest(
                denemeSinaviId,
                startedAt,
                finishedAt,
                items
        );

        System.out.println("🚀 Deneme sınavı submit başlatılıyor: ID=" + denemeSinaviId + ", Items=" + (items != null ? items.size() : 0));
        if (items != null && !items.isEmpty()) {
            System.out.println("📋 Parse edilen items:");
            for (int i = 0; i < Math.min(items.size(), 5); i++) {
                DenemeSinaviSubmitItemDTO item = items.get(i);
                System.out.println("  [" + i + "] SoruNo: " + item.soruNo() + ", Seçilen: '" + item.secilenCevap() + "'");
            }
            if (items.size() > 5) {
                System.out.println("  ... ve " + (items.size() - 5) + " tane daha");
            }
        } else {
            System.out.println("⚠️⚠️⚠️ UYARI: Items NULL veya BOŞ! Frontend cevapları göndermiyor!");
        }
        System.out.println("=".repeat(80) + "\n");
        
        return quizService.submitDenemeSinavi(request, user);
    }

    // Helper methods
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

    private Integer getInteger(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            String str = value.toString().trim();
            if (str.isEmpty()) return null;
            return Integer.parseInt(str);
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
}

