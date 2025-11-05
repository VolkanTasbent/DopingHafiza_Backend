// src/main/java/com/example/backend/controller/SoruController.java
package com.example.backend.controller;

import com.example.backend.dto.DenemeSinaviSoruDTO;
import com.example.backend.dto.KonuDTO;
import com.example.backend.dto.SecenekDTO;
import com.example.backend.dto.SoruDTO;
import com.example.backend.dto.UpdateDenemeSinaviSoruRequest;
import com.example.backend.dto.UpdateSecenekRequest;
import com.example.backend.dto.UpdateSoruRequest;
import com.example.backend.repository.DenemeSinaviSoruRepository;
import com.example.backend.repository.KonuRepository;
import com.example.backend.repository.SoruRepository;
import com.example.backend.service.DenemeSinaviService;
import com.example.backend.service.SoruService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sorular")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class SoruController {

    private final SoruService service;
    private final SoruRepository soruRepo;
    private final DenemeSinaviService denemeSinaviService;
    private final DenemeSinaviSoruRepository denemeSoruRepo;
    private final KonuRepository konuRepo;
    
    public SoruController(SoruService service, SoruRepository soruRepo, 
                          DenemeSinaviService denemeSinaviService,
                          DenemeSinaviSoruRepository denemeSoruRepo,
                          KonuRepository konuRepo) { 
        this.service = service;
        this.soruRepo = soruRepo;
        this.denemeSinaviService = denemeSinaviService;
        this.denemeSoruRepo = denemeSoruRepo;
        this.konuRepo = konuRepo;
    }

    @GetMapping
    public List<SoruDTO> liste(@RequestParam Long dersId,
                               @RequestParam(required = false) Long konuId,
                               @RequestParam(required = false) Integer limit,
                               @RequestParam(required = false) Integer page,
                               @RequestParam(required = false) Integer size) {
        // Her zaman deneme sınavı sorularını filtrele
        if (konuId != null) {
            return service.getSorularByKonu(dersId, konuId, limit, true);
        }
        if (page != null || size != null) {
            int p = page != null ? Math.max(0, page) : 0;
            int s = size != null ? Math.max(1, size) : 10;
            return service.getSorularPaged(dersId, p, s, true);
        }
        return service.getSorular(dersId, limit, true);
    }

    /** Tek soru getir (ID ile) - Hem normal sorular hem de deneme sınavı soruları için */
    @GetMapping("/{id}")
    public SoruDTO getById(@PathVariable Long id) {
        // Önce normal soru tablosunda ara
        if (soruRepo.existsById(id)) {
            return service.getById(id);
        }
        
        // Bulunamadıysa deneme sınavı soruları tablosunda ara
        if (denemeSoruRepo.existsById(id)) {
            DenemeSinaviSoruDTO denemeSoru = denemeSinaviService.getSoruById(id);
            return convertDenemeSoruToSoruDTO(denemeSoru);
        }
        
        throw new IllegalArgumentException("Soru bulunamadı: " + id);
    }
    
    /** Deneme sınavı sorusunu SoruDTO formatına dönüştür */
    private SoruDTO convertDenemeSoruToSoruDTO(DenemeSinaviSoruDTO denemeSoru) {
        // Şıkları SecenekDTO listesine çevir
        // DENEME SINAVI İÇİN: Fake ID = soruId * 1000 + sıralama
        // Örnek: Soru ID=109, Sıralama=1 (A) -> ID=109001
        // Bu sayede frontend secenekId gönderebilir
        List<SecenekDTO> secenekler = new ArrayList<>();
        int siralama = 1;
        if (denemeSoru.sikA() != null && !denemeSoru.sikA().trim().isEmpty()) {
            Long fakeId = denemeSoru.id() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, denemeSoru.sikA().trim(), 
                denemeSoru.dogruCevap() != null && denemeSoru.dogruCevap().equalsIgnoreCase("A"), siralama++));
        }
        if (denemeSoru.sikB() != null && !denemeSoru.sikB().trim().isEmpty()) {
            Long fakeId = denemeSoru.id() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, denemeSoru.sikB().trim(), 
                denemeSoru.dogruCevap() != null && denemeSoru.dogruCevap().equalsIgnoreCase("B"), siralama++));
        }
        if (denemeSoru.sikC() != null && !denemeSoru.sikC().trim().isEmpty()) {
            Long fakeId = denemeSoru.id() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, denemeSoru.sikC().trim(), 
                denemeSoru.dogruCevap() != null && denemeSoru.dogruCevap().equalsIgnoreCase("C"), siralama++));
        }
        if (denemeSoru.sikD() != null && !denemeSoru.sikD().trim().isEmpty()) {
            Long fakeId = denemeSoru.id() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, denemeSoru.sikD().trim(), 
                denemeSoru.dogruCevap() != null && denemeSoru.dogruCevap().equalsIgnoreCase("D"), siralama++));
        }
        if (denemeSoru.sikE() != null && !denemeSoru.sikE().trim().isEmpty()) {
            Long fakeId = denemeSoru.id() * 1000L + siralama;
            secenekler.add(new SecenekDTO(fakeId, denemeSoru.sikE().trim(), 
                denemeSoru.dogruCevap() != null && denemeSoru.dogruCevap().equalsIgnoreCase("E"), siralama++));
        }
        
        // Konuları parse et (virgülle ayrılmış string)
        List<KonuDTO> konular = new ArrayList<>();
        if (denemeSoru.konular() != null && !denemeSoru.konular().trim().isEmpty()) {
            String[] konuAdlari = denemeSoru.konular().split(",");
            for (String konuAdi : konuAdlari) {
                String trimmed = konuAdi.trim();
                if (!trimmed.isEmpty()) {
                    // Konu ID'si yok, sadece ad ile oluşturuyoruz
                    konular.add(new KonuDTO(null, trimmed, "", "", ""));
                }
            }
        }
        
        return new SoruDTO(
            denemeSoru.id(),
            denemeSoru.soruMetni() != null ? denemeSoru.soruMetni() : "",
            "coktan_secmeli", // Deneme sınavı soruları genelde çoktan seçmeli
            denemeSoru.zorluk(),
            denemeSoru.imageUrl() != null ? denemeSoru.imageUrl() : "", // imageUrl eklendi
            denemeSoru.dersAd() != null ? denemeSoru.dersAd() : "Bilinmeyen",
            konular,
            secenekler,
            denemeSoru.cozumVideosuUrl() != null ? denemeSoru.cozumVideosuUrl() : ""
        );
    }

    /** Soru oluşturma: konuIds dizi + (opsiyonel) secenekler dizisi aynı istekte gelir */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public SoruDTO create(@RequestBody Map<String, Object> body) {
        Long dersId = requiredLong(body, "dersId");
        List<Long> konuIds = requiredLongList(body, "konuIds");
        String metin = requiredString(body, "metin");
        String tip = optionalString(body, "tip");
        Integer zorluk = optionalInteger(body, "zorluk");
        String imageUrl = optionalString(body, "imageUrl");
        String aciklama = optionalString(body, "aciklama");
        String cozumVideosuUrl = optionalString(body, "cozumVideosuUrl");
        Integer soruNo = optionalInteger(body, "soruNo");

        SoruDTO created = service.addSoru(dersId, konuIds, metin, tip, zorluk, imageUrl, soruNo, aciklama, cozumVideosuUrl);

        // varsa secenekler ekle (sende zaten vardı)
        Object seceneklerObj = body.get("secenekler");
        if (seceneklerObj instanceof List<?> raw && !raw.isEmpty()) {
            for (Object o : raw) {
                if (o instanceof Map<?,?> m) {
                    String smetin = Objects.toString(m.get("metin"), "").trim();
                    boolean dogru = m.get("dogru") != null && Boolean.parseBoolean(Objects.toString(m.get("dogru")));
                    Integer siralama = optionalInteger((Map<String,Object>) (Map<?,?>) m, "siralama");
                    if (!smetin.isEmpty()) service.addSecenek(created.id(), smetin, dogru, siralama);
                }
            }
        }
        return service.getById(created.id());
    }

    @PostMapping("/{soruId}/secenekler")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SecenekDTO addSecenek(@PathVariable Long soruId, @RequestBody Map<String, Object> body) {
        String metin = requiredString(body, "metin");
        boolean dogru = body.get("dogru") != null && Boolean.parseBoolean(Objects.toString(body.get("dogru")));
        Integer siralama = optionalInteger(body, "siralama");
        return service.addSecenek(soruId, metin, dogru, siralama);
    }

    /** Soru güncelle (ADMIN) - Hem normal sorular hem de deneme sınavı soruları için */
    @PutMapping("/{soruId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Object updateSoru(@PathVariable Long soruId, 
                               @RequestBody Map<String, Object> body) {
        // DEBUG: Request body'yi logla
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📥 PUT /api/sorular/" + soruId + " - REQUEST BODY");
        System.out.println("📥 Keys: " + body.keySet());
        
        // Şıkları detaylı logla
        Object seceneklerObj = body.get("secenekler");
        if (seceneklerObj != null) {
            System.out.println("📥 Secenekler var (type: " + seceneklerObj.getClass().getSimpleName() + ")");
            if (seceneklerObj instanceof List<?> secenekList) {
                System.out.println("📥 Secenekler listesi boyutu: " + secenekList.size());
                for (int i = 0; i < secenekList.size() && i < 5; i++) {
                    System.out.println("  📥 Secenek[" + i + "]: " + secenekList.get(i));
                }
            } else {
                System.out.println("📥 Secenekler (toString): " + seceneklerObj);
            }
        } else {
            System.out.println("⚠️ Secenekler NULL - Frontend şıkları göndermiyor!");
        }
        System.out.println("=".repeat(80) + "\n");
        
        // Map'i UpdateSoruRequest'e çevir (frontend farklı field adları kullanabilir)
        UpdateSoruRequest req = mapToUpdateSoruRequest(body);
        
        // DEBUG: Parse edilen şıkları logla
        System.out.println("🔍 Parse edilen UpdateSoruRequest:");
        System.out.println("  🔍 secenekler: " + (req.secenekler() != null ? req.secenekler().size() + " adet" : "null"));
        if (req.secenekler() != null && !req.secenekler().isEmpty()) {
            for (int i = 0; i < req.secenekler().size() && i < 5; i++) {
                var sec = req.secenekler().get(i);
                System.out.println("  🔍 Secenek[" + i + "]: id=" + sec.id() + ", metin='" + sec.metin() + "', dogru=" + sec.dogru() + ", siralama=" + sec.siralama());
            }
        }
        
        // Önce deneme sınavı sorusu olup olmadığını kontrol et
        if (denemeSoruRepo.existsById(soruId)) {
            System.out.println("✅ Deneme sınavı sorusu tespit edildi (ID: " + soruId + ")");
            // Deneme sınavı sorusu ise, deneme sınavı endpoint'ine yönlendir
            // Frontend'in UpdateDenemeSinaviSoruRequest formatına çevirmesi gerekiyor
            // Ama backend'de otomatik dönüşüm yapalım
            return convertAndUpdateDenemeSoru(soruId, req);
        }
        
        // Normal soru güncelleme
        var soruEntity = soruRepo.findWithRelsById(soruId)
                .orElseThrow(() -> new IllegalArgumentException("Soru bulunamadı: " + soruId));
        
        Long dersId = soruEntity.getDers().getId(); // Ders değiştirilemez, mevcut ders kullanılır
        
        // Soru bilgilerini güncelle
        SoruDTO updated = service.updateSoru(
            soruId,
            dersId,
            req.konuIds(),   // null ise değiştirilmez
            req.metin(),
            req.tip(),
            req.zorluk(),
            req.imageUrl(),
            req.soruNo(),
            req.aciklama(),
            req.cozumVideosuUrl()  // null ise güncellenmez, string ise güncellenir (boş string null'a çevrilir)
        );

        // Seçenekleri güncelle (varsa)
        if (req.secenekler() != null && !req.secenekler().isEmpty()) {
            for (UpdateSecenekRequest secenekReq : req.secenekler()) {
                if (secenekReq.id() == null) {
                    // Yeni seçenek ekle
                    Boolean dogru = secenekReq.dogru() != null ? secenekReq.dogru() : false;
                    service.addSecenek(soruId, secenekReq.metin(), dogru, secenekReq.siralama());
                } else {
                    // Mevcut seçeneği güncelle
                    service.updateSecenek(secenekReq.id(), secenekReq.metin(), secenekReq.dogru(), secenekReq.siralama());
                }
            }
        }

        return service.getById(soruId);
    }
    
    /** UpdateSoruRequest'i UpdateDenemeSinaviSoruRequest'e çevir ve güncelle */
    private SoruDTO convertAndUpdateDenemeSoru(Long soruId, UpdateSoruRequest req) {
        // Mevcut deneme sınavı sorusunu al (konular ve dersId için)
        DenemeSinaviSoruDTO mevcutSoru = denemeSinaviService.getSoruById(soruId);
        
        // Deneme sınavı soruları için seçenekler farklı formatta (sikA, sikB, vb.)
        // Frontend'den gelen secenekler array'ini sikA, sikB, sikC, sikD, sikE'ye çevir
        // ÖNEMLI: Frontend null ID'li şıklar gönderiyor, array sırasına göre işle (index 0 = A, 1 = B, vb.)
        // Mevcut şıkları başlangıç değeri olarak al (eğer frontend şık göndermezse korunur)
        String sikA = mevcutSoru.sikA();
        String sikB = mevcutSoru.sikB();
        String sikC = mevcutSoru.sikC();
        String sikD = mevcutSoru.sikD();
        String sikE = mevcutSoru.sikE();
        String dogruCevap = mevcutSoru.dogruCevap();
        
        // Frontend'den şık gönderildiyse, güncelle
        // DEBUG: Şıkları logla
        System.out.println("🔍 Deneme sınavı sorusu güncelleme - Soru ID: " + soruId);
        System.out.println("🔍 Frontend'den gelen şıklar: " + (req.secenekler() != null ? req.secenekler().size() : 0) + " adet");
        
        boolean dogruCevapGuncellendi = false;
        if (req.secenekler() != null && !req.secenekler().isEmpty()) {
            int index = 0;
            for (UpdateSecenekRequest sec : req.secenekler()) {
                // Sıralama bilgisi varsa onu kullan, yoksa array index'ini kullan (1-based)
                int siralama = sec.siralama() != null ? sec.siralama() : (index + 1);
                
                // Metin boşsa null yap (şıkları sil), doluysa güncelle
                String metin = (sec.metin() != null && !sec.metin().trim().isEmpty()) 
                    ? sec.metin().trim() 
                    : null;
                
                System.out.println("  📝 Şık " + siralama + " (index " + index + "): metin='" + metin + "', dogru=" + sec.dogru());
                
                switch (siralama) {
                    case 1 -> sikA = metin;
                    case 2 -> sikB = metin;
                    case 3 -> sikC = metin;
                    case 4 -> sikD = metin;
                    case 5 -> sikE = metin;
                }
                
                // Doğru cevabı bul (sadece true olduğunda güncelle)
                if (sec.dogru() != null && sec.dogru()) {
                    char harf = (char)('A' + siralama - 1);
                    dogruCevap = String.valueOf(harf);
                    dogruCevapGuncellendi = true;
                    System.out.println("  ✅ Doğru cevap: " + dogruCevap);
                }
                
                index++;
            }
        }
        
        // Eğer doğru cevap güncellenmediyse, mevcut değeri koru
        if (!dogruCevapGuncellendi) {
            dogruCevap = mevcutSoru.dogruCevap();
        }
        
        System.out.println("🔍 Güncellenen şıklar: A='" + sikA + "', B='" + sikB + "', C='" + sikC + "', D='" + sikD + "', E='" + sikE + "'");
        System.out.println("🔍 Doğru cevap: " + dogruCevap);
        
        // Konuları virgülle ayrılmış string'e çevir
        String konularStr = mevcutSoru.konular(); // Mevcut konuları koru
        if (req.konuIds() != null && !req.konuIds().isEmpty()) {
            // Konu ID'lerinden konu adlarını al
            List<String> konuAdlari = konuRepo.findAllById(req.konuIds()).stream()
                    .map(k -> k.getAd())
                    .toList();
            if (!konuAdlari.isEmpty()) {
                konularStr = String.join(",", konuAdlari);
            }
        }
        
        // DersId'yi mevcut sorudan al
        Long dersId = mevcutSoru.dersId();
        
        UpdateDenemeSinaviSoruRequest denemeReq = new UpdateDenemeSinaviSoruRequest(
            req.metin() != null ? req.metin() : mevcutSoru.soruMetni(), // metin null ise mevcut değeri koru
            sikA,
            sikB,
            sikC,
            sikD,
            sikE,
            dogruCevap,
            req.zorluk() != null ? req.zorluk() : mevcutSoru.zorluk(), // zorluk null ise mevcut değeri koru
            konularStr,
            null, // aciklama - UpdateSoruRequest'te yok, mevcut değeri koru
            mevcutSoru.soruNo(), // soruNo - mevcut değeri koru
            dersId, // dersId - mevcut değeri koru
            req.cozumVideosuUrl() != null ? req.cozumVideosuUrl() : mevcutSoru.cozumVideosuUrl(), // cozumVideosuUrl null ise mevcut değeri koru
            req.imageUrl() != null ? req.imageUrl() : mevcutSoru.imageUrl() // imageUrl null ise mevcut değeri koru
        );
        
        DenemeSinaviSoruDTO updated = denemeSinaviService.updateSoru(soruId, denemeReq);
        
        // Güncellenmiş soruyu SoruDTO formatında döndür (normal soru formatı gibi)
        return convertDenemeSoruToSoruDTO(updated);
    }

    /** Seçenek güncelle (ADMIN) - Hem normal sorular hem de deneme sınavı soruları için */
    @PutMapping("/secenekler/{secenekId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Object updateSecenek(@PathVariable Long secenekId, 
                                     @Valid @RequestBody UpdateSecenekRequest req) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📥 PUT /api/sorular/secenekler/" + secenekId);
        System.out.println("📥 Request: metin='" + req.metin() + "', dogru=" + req.dogru() + ", siralama=" + req.siralama());
        
        // Eğer secenekId null ise veya fake ID formatındaysa (deneme sınavı soruları için)
        // Fake ID formatı: soruId * 1000 + siralama (örnek: 1001 = soru 1, şık 1)
        // Veya secenekId çok büyükse (fake ID olabilir)
        if (secenekId != null && secenekId > 1000) {
            Long soruId = secenekId / 1000;
            Integer siralama = (int)(secenekId % 1000);
            
            System.out.println("🔍 Fake ID tespit edildi: soruId=" + soruId + ", siralama=" + siralama);
            
            // Deneme sınavı sorusu kontrolü
            if (denemeSoruRepo.existsById(soruId)) {
                System.out.println("✅ Deneme sınavı sorusu tespit edildi (ID: " + soruId + ")");
                
                // Deneme sınavı soruları için şık güncelleme - gerçekten güncelle!
                // Mevcut soruyu al
                DenemeSinaviSoruDTO mevcutSoru = denemeSinaviService.getSoruById(soruId);
                
                // Şıkları güncelle
                String sikA = mevcutSoru.sikA();
                String sikB = mevcutSoru.sikB();
                String sikC = mevcutSoru.sikC();
                String sikD = mevcutSoru.sikD();
                String sikE = mevcutSoru.sikE();
                String dogruCevap = mevcutSoru.dogruCevap();
                
                // İlgili şıkkı güncelle
                String yeniMetin = (req.metin() != null && !req.metin().trim().isEmpty()) 
                    ? req.metin().trim() 
                    : null;
                
                switch (siralama) {
                    case 1 -> sikA = yeniMetin;
                    case 2 -> sikB = yeniMetin;
                    case 3 -> sikC = yeniMetin;
                    case 4 -> sikD = yeniMetin;
                    case 5 -> sikE = yeniMetin;
                }
                
                // Doğru cevabı güncelle (eğer bu şık doğruysa)
                if (req.dogru() != null && req.dogru()) {
                    char harf = (char)('A' + siralama - 1);
                    dogruCevap = String.valueOf(harf);
                    System.out.println("  ✅ Doğru cevap güncellendi: " + dogruCevap);
                } else if (req.dogru() != null && !req.dogru()) {
                    // Eğer bu şık artık doğru değilse ve bu şık önce doğru cevaptıysa, doğru cevabı koru
                    // (çünkü başka bir şık doğru olabilir)
                    // Bu durumda mevcut dogruCevap'ı koru (zaten mevcutSoru.dogruCevap() değeri var)
                    System.out.println("  ⚠️ Bu şık artık doğru değil, mevcut doğru cevap korunuyor: " + dogruCevap);
                }
                
                System.out.println("  📝 Şıklar güncelleniyor: A='" + sikA + "', B='" + sikB + "', C='" + sikC + "', D='" + sikD + "', E='" + sikE + "'");
                System.out.println("  📝 Doğru cevap: " + dogruCevap);
                
                // UpdateDenemeSinaviSoruRequest oluştur (sadece şıkları güncelle, diğer alanları koru)
                // NOT: dogruCevap null olamaz (@Pattern validasyonu var), bu yüzden mevcut değeri kullan
                UpdateDenemeSinaviSoruRequest denemeReq = new UpdateDenemeSinaviSoruRequest(
                    null, // metin - değiştirme
                    sikA,
                    sikB,
                    sikC,
                    sikD,
                    sikE,
                    dogruCevap != null ? dogruCevap : mevcutSoru.dogruCevap(), // null ise mevcut değeri koru
                    null, // zorluk - değiştirme
                    null, // konular - değiştirme
                    null, // aciklama - değiştirme
                    null, // soruNo - değiştirme
                    null, // dersId - değiştirme
                    null, // cozumVideosuUrl - değiştirme
                    null  // imageUrl - değiştirme
                );
                
                // Güncelle
                denemeSinaviService.updateSoru(soruId, denemeReq);
                System.out.println("✅ Deneme sınavı sorusu şıkları başarıyla güncellendi!");
                System.out.println("=".repeat(80) + "\n");
                
                // Fake SecenekDTO döndür (frontend'in beklediği format)
                return new SecenekDTO(secenekId, req.metin(), req.dogru() != null ? req.dogru() : false, siralama);
            }
        }
        
        // Normal soru şık güncelleme
        if (secenekId == null) {
            throw new IllegalArgumentException("Şık ID gerekli");
        }
        
        return service.updateSecenek(secenekId, req.metin(), req.dogru(), req.siralama());
    }

    /** Soru çözüm videosu URL'ini güncelle (ADMIN) - Hem normal sorular hem de deneme sınavı soruları için */
    @PatchMapping("/{soruId}/cozum-videosu")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Object updateCozumVideosu(@PathVariable Long soruId, 
                                       @RequestBody Map<String, String> body) {
        String cozumVideosuUrl = body.get("cozumVideosuUrl");
        if (cozumVideosuUrl != null && cozumVideosuUrl.length() > 500) {
            throw new IllegalArgumentException("Çözüm videosu URL maksimum 500 karakter olabilir");
        }
        
        // Deneme sınavı sorusu kontrolü
        if (denemeSoruRepo.existsById(soruId)) {
            // Deneme sınavı sorusu için video URL güncelleme
            String trimmed = cozumVideosuUrl != null ? cozumVideosuUrl.trim() : null;
            trimmed = (trimmed != null && !trimmed.isEmpty()) ? trimmed : null;
            return denemeSinaviService.updateSoruCozumVideosu(soruId, trimmed);
        }
        
        // Normal soru için video URL güncelleme
        var soruEntity = soruRepo.findWithRelsById(soruId)
                .orElseThrow(() -> new IllegalArgumentException("Soru bulunamadı: " + soruId));
        
        Long dersId = soruEntity.getDers().getId();
        
        // Sadece cozumVideosuUrl'yi güncelle
        service.updateSoru(soruId, dersId, null, null, null, null, null, null, null, cozumVideosuUrl);
        
        return service.getById(soruId);
    }

    @DeleteMapping("/secenekler/{secenekId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSecenek(@PathVariable Long secenekId) {
        service.deleteSecenek(secenekId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSoru(@PathVariable Long id) {
        service.deleteSoru(id);
    }

    // ---- helpers ----
    private static Long requiredLong(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) throw new IllegalArgumentException(key + " eksik");
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); }
        catch (Exception e) { throw new IllegalArgumentException(key + " sayısal olmalı"); }
    }

    @SuppressWarnings("unchecked")
    private static List<Long> requiredLongList(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) throw new IllegalArgumentException(key + " eksik");
        try {
            List<?> raw = (List<?>) v;
            if (raw.isEmpty()) throw new IllegalArgumentException(key + " boş olamaz");
            return raw.stream().map(o -> {
                if (o instanceof Number n) return n.longValue();
                return Long.parseLong(Objects.toString(o));
            }).toList();
        } catch (Exception e) {
            throw new IllegalArgumentException(key + " dizi (array) olmalı");
        }
    }

    private static String requiredString(Map<String, Object> body, String key) {
        String s = Objects.toString(body.get(key), "").trim();
        if (s.isEmpty()) throw new IllegalArgumentException(key + " boş olamaz");
        return s;
    }

    private static String optionalString(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : Objects.toString(v, null);
    }

    private static Integer optionalInteger(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); }
        catch (Exception e) { return null; }
    }
    
    /** Map'i UpdateSoruRequest'e çevir (frontend farklı field adları kullanabilir) */
    @SuppressWarnings("unchecked")
    private UpdateSoruRequest mapToUpdateSoruRequest(Map<String, Object> body) {
        String metin = optionalString(body, "metin");
        String tip = optionalString(body, "tip");
        Integer zorluk = optionalInteger(body, "zorluk");
        String imageUrl = optionalString(body, "imageUrl");
        String aciklama = optionalString(body, "aciklama");
        String cozumVideosuUrl = optionalString(body, "cozumVideosuUrl");
        Integer soruNo = optionalInteger(body, "soruNo");
        
        List<Long> konuIds = null;
        Object konuIdsObj = body.get("konuIds");
        if (konuIdsObj instanceof List<?>) {
            konuIds = ((List<?>) konuIdsObj).stream()
                    .map(o -> {
                        if (o instanceof Number n) return n.longValue();
                        try { return Long.parseLong(o.toString()); }
                        catch (Exception e) { return null; }
                    })
                    .filter(id -> id != null)
                    .toList();
        }
        
        List<UpdateSecenekRequest> secenekler = null;
        Object seceneklerObj = body.get("secenekler");
        if (seceneklerObj instanceof List<?> secenekList) {
            secenekler = secenekList.stream()
                    .map(o -> {
                        if (o instanceof Map<?, ?> m) {
                            Map<String, Object> secMap = (Map<String, Object>) m;
                            
                            // Frontend field adlarını backend field adlarına çevir
                            // text -> metin, correct -> dogru, order -> siralama, secenekId -> id
                            String metinValue = optionalString(secMap, "metin");
                            if (metinValue == null) metinValue = optionalString(secMap, "text");
                            
                            Boolean dogruValue = null;
                            Object dogruObj = secMap.get("dogru");
                            if (dogruObj == null) dogruObj = secMap.get("correct");
                            if (dogruObj != null) {
                                dogruValue = Boolean.parseBoolean(dogruObj.toString());
                            }
                            
                            Integer siralamaValue = optionalInteger(secMap, "siralama");
                            if (siralamaValue == null) siralamaValue = optionalInteger(secMap, "order");
                            
                            Long idValue = null;
                            Object idObj = secMap.get("id");
                            if (idObj == null) idObj = secMap.get("secenekId");
                            if (idObj != null) {
                                if (idObj instanceof Number n) idValue = n.longValue();
                                else try { idValue = Long.parseLong(idObj.toString()); } catch (Exception e) {}
                            }
                            
                            if (metinValue != null) {
                                return new UpdateSecenekRequest(idValue, metinValue, dogruValue, siralamaValue);
                            }
                        }
                        return null;
                    })
                    .filter(sec -> sec != null)
                    .toList();
        }
        
        return new UpdateSoruRequest(
            metin, tip, zorluk, imageUrl, aciklama, cozumVideosuUrl, soruNo, konuIds, secenekler
        );
    }
}
