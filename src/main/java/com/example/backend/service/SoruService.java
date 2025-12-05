package com.example.backend.service;

import com.example.backend.dto.KonuDTO;
import com.example.backend.dto.SecenekDTO;
import com.example.backend.dto.SoruDTO;
import com.example.backend.model.Ders;
import com.example.backend.model.Konu;
import com.example.backend.model.Secenek;
import com.example.backend.model.Soru;
import com.example.backend.repository.DenemeSinaviSoruRepository;
import com.example.backend.repository.DersRepository;
import com.example.backend.repository.KonuRepository;
import com.example.backend.repository.SecenekRepository;
import com.example.backend.repository.SoruRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SoruService {

    private final DersRepository dersRepo;
    private final KonuRepository konuRepo;
    private final SoruRepository soruRepo;
    private final SecenekRepository secenekRepo;
    private final DenemeSinaviSoruRepository denemeSoruRepo;

    public SoruService(DersRepository dersRepo,
                       KonuRepository konuRepo,
                       SoruRepository soruRepo,
                       SecenekRepository secenekRepo,
                       DenemeSinaviSoruRepository denemeSoruRepo) {
        this.dersRepo = dersRepo;
        this.konuRepo = konuRepo;
        this.soruRepo = soruRepo;
        this.secenekRepo = secenekRepo;
        this.denemeSoruRepo = denemeSoruRepo;
    }

    /** Ders bazlı listeleme (limit ile) - Deneme sınavı soruları hariç */
    @Transactional(readOnly = true)
    public List<SoruDTO> getSorular(Long dersId, Integer limit, boolean filterDenemeSorular) {
        Ders ders = dersRepo.findById(dersId)
                .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
        int lim = (limit != null && limit > 0) ? limit : 50;
        var list = soruRepo.findByDersOrderByIdAsc(ders, PageRequest.of(0, lim));
        
        // Admin kullanıcılar için filtreleme yapma
        if (!filterDenemeSorular) {
            return list.stream().map(this::toDTO).toList();
        }
        
        // Deneme sınavındaki tüm soru metinlerini al (normalize edilmiş - trim, lowercase)
        Set<String> denemeSoruMetinleri = denemeSoruRepo.findAll().stream()
                .map(ds -> ds.getSoruMetni() != null ? ds.getSoruMetni().trim().toLowerCase() : "")
                .filter(metin -> !metin.isEmpty())
                .collect(Collectors.toSet());
        
        // Sadece deneme sınavında OLMAYAN soruları döndür (soru metnine göre kontrol)
        return list.stream()
                .filter(s -> {
                    if (s.getMetin() == null) return true; // Metin yoksa geç
                    String soruMetni = s.getMetin().trim().toLowerCase();
                    return !denemeSoruMetinleri.contains(soruMetni); // Deneme sınavında olmayanları al
                })
                .map(this::toDTO)
                .toList();
    }

    /** Ders + Konu bazlı listeleme (limit ile) - Deneme sınavı soruları hariç */
    @Transactional(readOnly = true)
    public List<SoruDTO> getSorularByKonu(Long dersId, Long konuId, Integer limit, boolean filterDenemeSorular) {
        Ders ders = dersRepo.findById(dersId)
                .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
        int lim = (limit != null && limit > 0) ? limit : 50;
        var list = soruRepo.findDistinctByDersAndKonular_IdOrderByIdAsc(ders, konuId, PageRequest.of(0, lim));
        
        // Admin kullanıcılar için filtreleme yapma
        if (!filterDenemeSorular) {
            return list.stream().map(this::toDTO).toList();
        }
        
        // Deneme sınavındaki tüm soru metinlerini al (normalize edilmiş - trim, lowercase)
        Set<String> denemeSoruMetinleri = denemeSoruRepo.findAll().stream()
                .map(ds -> ds.getSoruMetni() != null ? ds.getSoruMetni().trim().toLowerCase() : "")
                .filter(metin -> !metin.isEmpty())
                .collect(Collectors.toSet());
        
        // Sadece deneme sınavında OLMAYAN soruları döndür (soru metnine göre kontrol)
        return list.stream()
                .filter(s -> {
                    if (s.getMetin() == null) return true; // Metin yoksa geç
                    String soruMetni = s.getMetin().trim().toLowerCase();
                    return !denemeSoruMetinleri.contains(soruMetni); // Deneme sınavında olmayanları al
                })
                .map(this::toDTO)
                .toList();
    }

    /** Ders bazlı sayfalı listeleme - Deneme sınavı soruları hariç */
    @Transactional(readOnly = true)
    public List<SoruDTO> getSorularPaged(Long dersId, int page, int size, boolean filterDenemeSorular) {
        Ders ders = dersRepo.findById(dersId)
                .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
        var list = soruRepo.findByDersOrderByIdAsc(ders, PageRequest.of(Math.max(0, page), Math.max(1, size)));
        
        // Admin kullanıcılar için filtreleme yapma
        if (!filterDenemeSorular) {
            return list.stream().map(this::toDTO).toList();
        }
        
        // Deneme sınavındaki tüm soru metinlerini al (normalize edilmiş - trim, lowercase)
        Set<String> denemeSoruMetinleri = denemeSoruRepo.findAll().stream()
                .map(ds -> ds.getSoruMetni() != null ? ds.getSoruMetni().trim().toLowerCase() : "")
                .filter(metin -> !metin.isEmpty())
                .collect(Collectors.toSet());
        
        // Sadece deneme sınavında OLMAYAN soruları döndür (soru metnine göre kontrol)
        return list.stream()
                .filter(s -> {
                    if (s.getMetin() == null) return true; // Metin yoksa geç
                    String soruMetni = s.getMetin().trim().toLowerCase();
                    return !denemeSoruMetinleri.contains(soruMetni); // Deneme sınavında olmayanları al
                })
                .map(this::toDTO)
                .toList();
    }

    /** Eski imza (geriye dönük uyum) */
    @Transactional
    public SoruDTO addSoru(Long dersId, List<Long> konuIds, String metin, String tip, Integer zorluk, String imageUrl) {
        return addSoru(dersId, konuIds, metin, tip, zorluk, imageUrl, null, null, null);
    }

    /** Yeni: aciklama + soruNo + cozumVideosuUrl destekli */
    @Transactional
    public SoruDTO addSoru(Long dersId, List<Long> konuIds, String metin, String tip, Integer zorluk,
                           String imageUrl, Integer soruNo, String aciklama, String cozumVideosuUrl) {
        if (metin == null || metin.isBlank()) throw new IllegalArgumentException("Soru metni boş olamaz");
        if (konuIds == null || konuIds.isEmpty()) throw new IllegalArgumentException("En az bir konu seçmelisiniz");

        Ders ders = dersRepo.findById(dersId)
                .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));

        var konular = new LinkedHashSet<Konu>(konuRepo.findAllById(konuIds));
        if (konular.isEmpty() || konular.size() != new LinkedHashSet<>(konuIds).size()) {
            throw new IllegalArgumentException("Geçersiz konu listesi");
        }
        boolean hepsiAit = konular.stream().allMatch(k -> k.getDers().getId().equals(ders.getId()));
        if (!hepsiAit) throw new IllegalArgumentException("Seçilen konular, belirtilen derse ait değil");

        Soru s = new Soru();
        s.setDers(ders);
        s.setKonular(konular);
        s.setMetin(metin.trim());
        s.setTip(tip);
        s.setZorluk(zorluk);
        s.setImageUrl(imageUrl);
        s.setAciklama(aciklama);
        s.setCozumVideosuUrl(cozumVideosuUrl);

        // soruNo verilmemişse ders içi max + 1
        Integer currentMax = soruRepo.findMaxSoruNoByDers(ders);
        s.setSoruNo(soruNo != null ? soruNo : ((currentMax == null ? 0 : currentMax) + 1));

        s = soruRepo.save(s);
        // İlişkileri eager yüklenmiş entity üzerinden DTO dön
        return getById(s.getId());
    }

    /** ID ile getir (ilişkiler eager) */
    @Transactional(readOnly = true)
    public SoruDTO getById(Long id) {
        Soru s = soruRepo.findWithRelsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Soru bulunamadı: " + id));
        return toDTO(s);
    }

    /** Seçenek ekle */
    @Transactional
    public SecenekDTO addSecenek(Long soruId, String metin, boolean dogru, Integer siralama) {
        if (metin == null || metin.isBlank()) throw new IllegalArgumentException("Seçenek metni boş olamaz");
        Soru soru = soruRepo.findWithRelsById(soruId)
                .orElseThrow(() -> new IllegalArgumentException("Soru bulunamadı: " + soruId));
        Secenek o = new Secenek();
        o.setSoru(soru);
        o.setMetin(metin.trim());
        o.setDogru(dogru);
        o.setSiralama(siralama);
        o = secenekRepo.save(o);
        return new SecenekDTO(o.getId(), o.getMetin() != null ? o.getMetin() : "", o.isDogru(), o.getSiralama());
    }

    /** Seçenek sil */
    @Transactional
    public void deleteSecenek(Long id) {
        if (!secenekRepo.existsById(id)) {
            throw new IllegalArgumentException("Seçenek bulunamadı: " + id);
        }
        secenekRepo.deleteById(id);
    }

    /** Soru güncelle */
    @Transactional
    public SoruDTO updateSoru(Long soruId, Long dersId, List<Long> konuIds, String metin, 
                              String tip, Integer zorluk, String imageUrl, 
                              Integer soruNo, String aciklama, String cozumVideosuUrl) {
        Soru s = soruRepo.findWithRelsById(soruId)
                .orElseThrow(() -> new IllegalArgumentException("Soru bulunamadı: " + soruId));

        // Konu listesi güncelle (varsa)
        if (konuIds != null && !konuIds.isEmpty()) {
            var konular = new LinkedHashSet<Konu>(konuRepo.findAllById(konuIds));
            if (konular.isEmpty() || konular.size() != new LinkedHashSet<>(konuIds).size()) {
                throw new IllegalArgumentException("Geçersiz konu listesi");
            }
            // Ders kontrolü
            Ders ders = dersRepo.findById(dersId)
                    .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
            boolean hepsiAit = konular.stream().allMatch(k -> k.getDers().getId().equals(ders.getId()));
            if (!hepsiAit) throw new IllegalArgumentException("Seçilen konular, belirtilen derse ait değil");
            
            s.setKonular(konular);
        }

        // Diğer alanları güncelle (null değilse)
        if (metin != null && !metin.isBlank()) {
            s.setMetin(metin.trim());
        }
        if (tip != null) {
            s.setTip(tip);
        }
        if (zorluk != null) {
            if (zorluk < 1 || zorluk > 5) {
                throw new IllegalArgumentException("Zorluk 1-5 arası olmalı");
            }
            s.setZorluk(zorluk);
        }
        if (imageUrl != null) {
            s.setImageUrl(imageUrl);
        }
        if (aciklama != null) {
            s.setAciklama(aciklama);
        }
        if (cozumVideosuUrl != null) {
            // Boş string ise null yap (URL'i temizle), aksi halde trim et ve kaydet
            String trimmed = cozumVideosuUrl.trim();
            s.setCozumVideosuUrl(trimmed.isEmpty() ? null : trimmed);
        }
        if (soruNo != null) {
            s.setSoruNo(soruNo);
        }

        s = soruRepo.save(s);
        return getById(s.getId());
    }

    /** Seçenek güncelle */
    @Transactional
    public SecenekDTO updateSecenek(Long secenekId, String metin, Boolean dogru, Integer siralama) {
        Secenek o = secenekRepo.findById(secenekId)
                .orElseThrow(() -> new IllegalArgumentException("Seçenek bulunamadı: " + secenekId));
        
        if (metin != null && !metin.isBlank()) {
            o.setMetin(metin.trim());
        }
        if (dogru != null) {
            o.setDogru(dogru);
        }
        if (siralama != null) {
            o.setSiralama(siralama);
        }
        
        o = secenekRepo.save(o);
        return new SecenekDTO(o.getId(), o.getMetin() != null ? o.getMetin() : "", o.isDogru(), o.getSiralama());
    }

    /** Soru sil */
    @Transactional
    public void deleteSoru(Long id) {
        if (!soruRepo.existsById(id)) {
            throw new IllegalArgumentException("Soru bulunamadı: " + id);
        }
        soruRepo.deleteById(id);
    }

    /** CSV'den toplu soru yükleme */
    @Transactional
    public Map<String, Object> importFromCSV(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Header'ı oku (ilk satırı atla)
            String header = reader.readLine();
            if (header == null) {
                throw new IllegalArgumentException("CSV dosyası boş");
            }
            lineNumber++;

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    // CSV parsing
                    List<String> fields = parseCSVLine(line);

                    if (fields.size() < 9) {
                        errors.add("Satır " + lineNumber + ": Yetersiz alan sayısı (en az 9 alan gerekli)");
                        continue;
                    }

                    String soruMetni = cleanField(fields.get(0));
                    String sikA = cleanField(fields.get(1));
                    String sikB = cleanField(fields.get(2));
                    String sikC = cleanField(fields.get(3));
                    String sikD = cleanField(fields.get(4));
                    String sikE = fields.size() > 5 ? cleanField(fields.get(5)) : "";
                    String dogruCevap = cleanField(fields.get(6));
                    String zorlukStr = cleanField(fields.get(7));
                    String konularStr = cleanField(fields.get(8));
                    String dersAd = fields.size() > 9 ? cleanField(fields.get(9)) : "";
                    String aciklama = fields.size() > 10 ? cleanField(fields.get(10)) : "";
                    String imageUrl = fields.size() > 11 ? cleanField(fields.get(11)) : "";
                    String cozumVideosuUrl = fields.size() > 12 ? cleanField(fields.get(12)) : "";

                    if (soruMetni.isBlank()) {
                        errors.add("Satır " + lineNumber + ": Soru metni boş");
                        continue;
                    }

                    if (dersAd.isBlank()) {
                        errors.add("Satır " + lineNumber + ": Ders adı boş");
                        continue;
                    }

                    if (!dogruCevap.matches("^[ABCDEabcde]$")) {
                        errors.add("Satır " + lineNumber + ": Geçersiz doğru cevap: " + dogruCevap);
                        continue;
                    }
                    // Büyük harfe çevir
                    dogruCevap = dogruCevap.toUpperCase();

                    // Ders bul
                    Ders ders = dersRepo.findAll().stream()
                            .filter(d -> d.getAd().equalsIgnoreCase(dersAd.trim()))
                            .findFirst()
                            .orElse(null);
                    if (ders == null) {
                        errors.add("Satır " + lineNumber + ": Ders bulunamadı: " + dersAd);
                        continue;
                    }

                    // Konuları bul
                    List<Long> konuIds = new ArrayList<>();
                    if (!konularStr.isBlank()) {
                        String[] konuAdlari = konularStr.split(",");
                        for (String konuAd : konuAdlari) {
                            String trimmedKonuAd = konuAd.trim();
                            if (!trimmedKonuAd.isEmpty()) {
                                Konu konu = konuRepo.findByDersOrderByAdAsc(ders).stream()
                                        .filter(k -> k.getAd().equalsIgnoreCase(trimmedKonuAd))
                                        .findFirst()
                                        .orElse(null);
                                if (konu != null) {
                                    konuIds.add(konu.getId());
                                } else {
                                    errors.add("Satır " + lineNumber + ": Konu bulunamadı: " + trimmedKonuAd + " (Ders: " + dersAd + ")");
                                }
                            }
                        }
                    }

                    if (konuIds.isEmpty()) {
                        errors.add("Satır " + lineNumber + ": En az bir konu gerekli");
                        continue;
                    }

                    // Zorluk parse et
                    Integer zorluk = null;
                    if (!zorlukStr.isBlank()) {
                        try {
                            zorluk = Integer.parseInt(zorlukStr);
                            if (zorluk < 1 || zorluk > 5) {
                                zorluk = null;
                            }
                        } catch (NumberFormatException e) {
                            // Zorluk yoksa null kalır
                        }
                    }

                    // Soru oluştur
                    SoruDTO soruDTO = addSoru(ders.getId(), konuIds, soruMetni, "coktan_secmeli", 
                            zorluk, imageUrl.isEmpty() ? null : imageUrl, null, 
                            aciklama.isEmpty() ? null : aciklama, 
                            cozumVideosuUrl.isEmpty() ? null : cozumVideosuUrl);

                    // Seçenekleri ekle
                    int siralama = 1;
                    if (!sikA.isBlank()) {
                        addSecenek(soruDTO.id(), sikA, dogruCevap.equals("A"), siralama++);
                    }
                    if (!sikB.isBlank()) {
                        addSecenek(soruDTO.id(), sikB, dogruCevap.equals("B"), siralama++);
                    }
                    if (!sikC.isBlank()) {
                        addSecenek(soruDTO.id(), sikC, dogruCevap.equals("C"), siralama++);
                    }
                    if (!sikD.isBlank()) {
                        addSecenek(soruDTO.id(), sikD, dogruCevap.equals("D"), siralama++);
                    }
                    if (!sikE.isBlank()) {
                        addSecenek(soruDTO.id(), sikE, dogruCevap.equals("E"), siralama++);
                    }

                    successCount++;

                } catch (Exception e) {
                    errors.add("Satır " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("CSV okuma hatası: " + e.getMessage());
        }

        return Map.of(
            "success", true,
            "successCount", successCount,
            "errorCount", errors.size(),
            "errors", errors
        );
    }

    /** CSV satırını parse et (basit implementasyon - tırnak desteği ile) */
    private List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Çift tırnak (escape)
                    current.append('"');
                    i++; // Bir sonraki karakteri atla
                } else {
                    // Tırnak aç/kapat
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Alan ayırıcı
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString()); // Son alan

        return fields;
    }

    private String cleanField(String field) {
        if (field == null) return "";
        return field.trim();
    }

    // ---- Dönüşüm ----
    private SoruDTO toDTO(Soru s) {
        var konuDtos = s.getKonular().stream()
                .map(k -> new KonuDTO(
                    k.getId(), 
                    k.getAd() != null ? k.getAd() : "", 
                    k.getDokumanUrl() != null ? k.getDokumanUrl() : "", 
                    k.getDokumanAdi() != null ? k.getDokumanAdi() : "", 
                    k.getKonuAnlatimVideosuUrl() != null ? k.getKonuAnlatimVideosuUrl() : ""))
                .toList();

        var opts = secenekRepo.findBySoruOrderBySiralamaAscIdAsc(s).stream()
                .map(o -> new SecenekDTO(o.getId(), o.getMetin() != null ? o.getMetin() : "", o.isDogru(), o.getSiralama()))
                .toList();

        return new SoruDTO(
                s.getId(),
                s.getMetin() != null ? s.getMetin() : "",
                s.getTip() != null ? s.getTip() : "",
                s.getZorluk(),
                s.getImageUrl() != null ? s.getImageUrl() : "",
                s.getDers() != null && s.getDers().getAd() != null ? s.getDers().getAd() : "",
                konuDtos,
                opts,
                s.getCozumVideosuUrl() != null ? s.getCozumVideosuUrl() : ""
        );
    }
}
