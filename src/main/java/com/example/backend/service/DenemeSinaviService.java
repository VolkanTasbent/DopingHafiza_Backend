package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.DenemeSinavi;
import com.example.backend.model.DenemeSinaviSoru;
import com.example.backend.model.Ders;
import com.example.backend.repository.DenemeSinaviRepository;
import com.example.backend.repository.DenemeSinaviSoruRepository;
import com.example.backend.repository.DersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class DenemeSinaviService {
    private final DenemeSinaviRepository denemeRepo;
    private final DenemeSinaviSoruRepository soruRepo;
    private final DersRepository dersRepo;

    public DenemeSinaviService(DenemeSinaviRepository denemeRepo, DenemeSinaviSoruRepository soruRepo, DersRepository dersRepo) {
        this.denemeRepo = denemeRepo;
        this.soruRepo = soruRepo;
        this.dersRepo = dersRepo;
    }

    /** Tüm deneme sınavlarını listele */
    @Transactional(readOnly = true)
    public List<DenemeSinaviDTO> listAll() {
        return denemeRepo.findAllByOrderByOlusturmaTarihiDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    /** Tipe göre listele (TYT/AYT) */
    @Transactional(readOnly = true)
    public List<DenemeSinaviDTO> listByTip(String tip) {
        return denemeRepo.findByTipOrderByOlusturmaTarihiDesc(tip).stream()
                .map(this::toDTO)
                .toList();
    }

    /** ID ile getir */
    @Transactional(readOnly = true)
    public DenemeSinaviDTO getById(Long id) {
        DenemeSinavi ds = denemeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + id));
        return toDTO(ds);
    }

    /** Deneme sınavı oluştur */
    @Transactional
    public DenemeSinaviDTO create(CreateDenemeSinaviRequest req) {
        DenemeSinavi ds = new DenemeSinavi();
        ds.setAd(req.ad().trim());
        ds.setTip(req.tip().toUpperCase());
        ds.setAciklama(req.aciklama());
        ds = denemeRepo.save(ds);
        return toDTO(ds);
    }

    /** Deneme sınavını güncelle */
    @Transactional
    public DenemeSinaviDTO update(Long id, CreateDenemeSinaviRequest req) {
        DenemeSinavi ds = denemeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + id));
        ds.setAd(req.ad().trim());
        ds.setTip(req.tip().toUpperCase());
        ds.setAciklama(req.aciklama());
        ds = denemeRepo.save(ds);
        return toDTO(ds);
    }

    /** Deneme sınavını sil */
    @Transactional
    public void delete(Long id) {
        DenemeSinavi ds = denemeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + id));
        soruRepo.deleteByDenemeSinavi(ds); // İlişkili soruları sil
        denemeRepo.delete(ds);
    }

    /** Tüm deneme sınavı sorularını getir - SoruDTO formatında (Admin panel için) */
    @Transactional(readOnly = true)
    public List<com.example.backend.dto.SoruDTO> getAllSorularAsSoruDTO() {
        return soruRepo.findAll().stream()
                .sorted((a, b) -> {
                    // Önce deneme sınavı ID'sine göre, sonra soru numarasına göre sırala
                    Long denemeIdA = a.getDenemeSinavi() != null ? a.getDenemeSinavi().getId() : 0L;
                    Long denemeIdB = b.getDenemeSinavi() != null ? b.getDenemeSinavi().getId() : 0L;
                    int denemeCompare = denemeIdA.compareTo(denemeIdB);
                    if (denemeCompare != 0) return denemeCompare;
                    return Integer.compare(
                        a.getSoruNo() != null ? a.getSoruNo() : 0,
                        b.getSoruNo() != null ? b.getSoruNo() : 0
                    );
                })
                .map(this::convertDenemeSoruToSoruDTO)
                .toList();
    }
    
    /** DenemeSinaviSoru'yu SoruDTO'ya çevir (helper method) */
    private com.example.backend.dto.SoruDTO convertDenemeSoruToSoruDTO(DenemeSinaviSoru s) {
        // Şıkları SecenekDTO listesine çevir
        // DENEME SINAVI İÇİN: Fake ID = soruId * 1000 + sıralama
        List<com.example.backend.dto.SecenekDTO> secenekler = new ArrayList<>();
        int siralama = 1;
        if (s.getSikA() != null && !s.getSikA().trim().isEmpty()) {
            Long fakeId = s.getId() * 1000L + siralama;
            secenekler.add(new com.example.backend.dto.SecenekDTO(fakeId, s.getSikA().trim(), 
                s.getDogruCevap() != null && s.getDogruCevap().equalsIgnoreCase("A"), siralama++));
        }
        if (s.getSikB() != null && !s.getSikB().trim().isEmpty()) {
            Long fakeId = s.getId() * 1000L + siralama;
            secenekler.add(new com.example.backend.dto.SecenekDTO(fakeId, s.getSikB().trim(), 
                s.getDogruCevap() != null && s.getDogruCevap().equalsIgnoreCase("B"), siralama++));
        }
        if (s.getSikC() != null && !s.getSikC().trim().isEmpty()) {
            Long fakeId = s.getId() * 1000L + siralama;
            secenekler.add(new com.example.backend.dto.SecenekDTO(fakeId, s.getSikC().trim(), 
                s.getDogruCevap() != null && s.getDogruCevap().equalsIgnoreCase("C"), siralama++));
        }
        if (s.getSikD() != null && !s.getSikD().trim().isEmpty()) {
            Long fakeId = s.getId() * 1000L + siralama;
            secenekler.add(new com.example.backend.dto.SecenekDTO(fakeId, s.getSikD().trim(), 
                s.getDogruCevap() != null && s.getDogruCevap().equalsIgnoreCase("D"), siralama++));
        }
        if (s.getSikE() != null && !s.getSikE().trim().isEmpty()) {
            Long fakeId = s.getId() * 1000L + siralama;
            secenekler.add(new com.example.backend.dto.SecenekDTO(fakeId, s.getSikE().trim(), 
                s.getDogruCevap() != null && s.getDogruCevap().equalsIgnoreCase("E"), siralama++));
        }
        
        // Konuları parse et (virgülle ayrılmış string)
        List<com.example.backend.dto.KonuDTO> konular = new ArrayList<>();
        if (s.getKonular() != null && !s.getKonular().trim().isEmpty()) {
            String[] konuAdlari = s.getKonular().split(",");
            for (String konuAdi : konuAdlari) {
                String trimmed = konuAdi.trim();
                if (!trimmed.isEmpty()) {
                    // KonuDTO: id, ad, dokumanUrl, dokumanAdi, konuAnlatimVideosuUrl, aciklama, dersId, videolar
                    konular.add(new com.example.backend.dto.KonuDTO(null, trimmed, "", "", "", null, null, java.util.Collections.emptyList()));
                }
            }
        }
        
        return new com.example.backend.dto.SoruDTO(
            s.getId(),
            s.getSoruMetni() != null ? s.getSoruMetni() : "",
            "coktan_secmeli",
            s.getZorluk(),
            s.getImageUrl() != null ? s.getImageUrl() : "",
            s.getDers() != null && s.getDers().getAd() != null ? s.getDers().getAd() : "",
            konular,
            secenekler,
            s.getCozumVideosuUrl() != null ? s.getCozumVideosuUrl() : ""
        );
    }

    /** Deneme sınavı sorularını getir - SoruDTO formatında (seçenekler dahil) */
    @Transactional(readOnly = true)
    public List<com.example.backend.dto.SoruDTO> getSorularAsSoruDTO(Long denemeId) {
        DenemeSinavi ds = denemeRepo.findById(denemeId)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + denemeId));
        
        return soruRepo.findByDenemeSinaviOrderBySoruNoAsc(ds).stream()
                .map(this::convertDenemeSoruToSoruDTO)
                .toList();
    }

    /** Deneme sınavı sorularını getir - Eski format (geriye dönük uyumluluk için) */
    @Transactional(readOnly = true)
    public List<DenemeSinaviSoruDTO> getSorular(Long denemeId) {
        DenemeSinavi ds = denemeRepo.findById(denemeId)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + denemeId));
        return soruRepo.findByDenemeSinaviOrderBySoruNoAsc(ds).stream()
                .map(this::soruToDTO)
                .toList();
    }

    /** Soru ekle */
    @Transactional
    public DenemeSinaviSoruDTO addSoru(Long denemeId, Long dersId, String soruMetni, String sikA, String sikB,
                                       String sikC, String sikD, String sikE, String dogruCevap,
                                       Integer zorluk, String konular, String aciklama, Integer soruNo) {
        DenemeSinavi ds = denemeRepo.findById(denemeId)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + denemeId));

        if (dogruCevap == null || !dogruCevap.matches("^[ABCDE]$")) {
            throw new IllegalArgumentException("Doğru cevap A, B, C, D veya E olmalıdır");
        }

        Ders ders = null;
        if (dersId != null) {
            ders = dersRepo.findById(dersId)
                    .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
        }

        DenemeSinaviSoru soru = new DenemeSinaviSoru();
        soru.setDenemeSinavi(ds);
        soru.setDers(ders);
        soru.setSoruMetni(soruMetni.trim());
        soru.setSikA(sikA);
        soru.setSikB(sikB);
        soru.setSikC(sikC);
        soru.setSikD(sikD);
        soru.setSikE(sikE);
        soru.setDogruCevap(dogruCevap.toUpperCase());
        soru.setZorluk(zorluk);
        soru.setKonular(konular);
        soru.setAciklama(aciklama);

        // Soru numarası belirtilmemişse otomatik ata
        if (soruNo == null) {
            Integer maxNo = soruRepo.findMaxSoruNoByDenemeSinavi(ds);
            soruNo = (maxNo == null ? 0 : maxNo) + 1;
        }
        soru.setSoruNo(soruNo);

        soru = soruRepo.save(soru);
        return soruToDTO(soru);
    }

    /** Soru güncelle */
    @Transactional
    public DenemeSinaviSoruDTO updateSoru(Long soruId, UpdateDenemeSinaviSoruRequest req) {
        DenemeSinaviSoru soru = soruRepo.findById(soruId)
                .orElseThrow(() -> new IllegalArgumentException("Soru bulunamadı: " + soruId));

        if (req.soruMetni() != null && !req.soruMetni().isBlank()) {
            soru.setSoruMetni(req.soruMetni().trim());
        }
        // Şıkları her zaman güncelle (null ise sil, dolu ise güncelle)
        // NOT: Bu sayede frontend boş şıkları silmek için null gönderebilir
        soru.setSikA(req.sikA());
        soru.setSikB(req.sikB());
        soru.setSikC(req.sikC());
        soru.setSikD(req.sikD());
        soru.setSikE(req.sikE());
        if (req.dogruCevap() != null) {
            soru.setDogruCevap(req.dogruCevap().toUpperCase());
        }
        if (req.zorluk() != null) {
            if (req.zorluk() < 1 || req.zorluk() > 5) {
                throw new IllegalArgumentException("Zorluk 1-5 arası olmalı");
            }
            soru.setZorluk(req.zorluk());
        }
        if (req.konular() != null) soru.setKonular(req.konular());
        if (req.aciklama() != null) soru.setAciklama(req.aciklama());
        if (req.soruNo() != null) soru.setSoruNo(req.soruNo());
        if (req.dersId() != null) {
            Ders ders = dersRepo.findById(req.dersId())
                    .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + req.dersId()));
            soru.setDers(ders);
        }
        if (req.cozumVideosuUrl() != null) {
            String trimmed = req.cozumVideosuUrl().trim();
            soru.setCozumVideosuUrl(trimmed.isEmpty() ? null : trimmed);
        }
        if (req.imageUrl() != null) {
            String trimmed = req.imageUrl().trim();
            soru.setImageUrl(trimmed.isEmpty() ? null : trimmed);
        }

        soru = soruRepo.save(soru);
        return soruToDTO(soru);
    }

    /** Soru sil */
    @Transactional
    public void deleteSoru(Long soruId) {
        if (!soruRepo.existsById(soruId)) {
            throw new IllegalArgumentException("Soru bulunamadı: " + soruId);
        }
        soruRepo.deleteById(soruId);
    }

    /** Deneme sınavı sorusu çözüm videosu URL'ini güncelle */
    @Transactional
    public DenemeSinaviSoruDTO updateSoruCozumVideosu(Long soruId, String videoUrl) {
        DenemeSinaviSoru soru = soruRepo.findById(soruId)
                .orElseThrow(() -> new IllegalArgumentException("Soru bulunamadı: " + soruId));
        
        String trimmed = videoUrl != null ? videoUrl.trim() : null;
        soru.setCozumVideosuUrl(trimmed != null && !trimmed.isEmpty() ? trimmed : null);
        
        soru = soruRepo.save(soru);
        return soruToDTO(soru);
    }

    /** CSV'den toplu soru yükleme */
    @Transactional
    public Map<String, Object> importFromCSV(Long denemeId, MultipartFile file) {
        DenemeSinavi ds = denemeRepo.findById(denemeId)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + denemeId));

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
                    // CSV parsing - virgülle ayrılmış, tırnak içinde alanlar
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
                    String sikE = cleanField(fields.size() > 5 ? fields.get(5) : "");
                    String dogruCevap = cleanField(fields.get(6));
                    String zorlukStr = cleanField(fields.get(7));
                    String konular = cleanField(fields.get(8));
                    String aciklama = fields.size() > 9 ? cleanField(fields.get(9)) : "";
                    String dersAd = fields.size() > 10 ? cleanField(fields.get(10)) : "";

                    if (soruMetni.isBlank()) {
                        errors.add("Satır " + lineNumber + ": Soru metni boş");
                        continue;
                    }

                    if (!dogruCevap.matches("^[ABCDE]$")) {
                        errors.add("Satır " + lineNumber + ": Geçersiz doğru cevap: " + dogruCevap);
                        continue;
                    }

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

                    // Ders adından ID bul
                    Long dersId = null;
                    if (!dersAd.isBlank()) {
                        dersId = dersRepo.findAll().stream()
                                .filter(d -> d.getAd().equalsIgnoreCase(dersAd.trim()))
                                .map(Ders::getId)
                                .findFirst()
                                .orElse(null);
                        if (dersId == null) {
                            errors.add("Satır " + lineNumber + ": Ders bulunamadı: " + dersAd);
                            continue;
                        }
                    }

                    addSoru(denemeId, dersId, soruMetni, sikA, sikB, sikC, sikD, sikE,
                            dogruCevap.toUpperCase(), zorluk, konular, aciklama, null);

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
            "denemeId", denemeId,
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

    /** Deneme sınavı sorularını quiz için getir - SoruDTO formatında (seçenekler dahil) */
    @Transactional(readOnly = true)
    public List<com.example.backend.dto.SoruDTO> getSorularForQuizAsSoruDTO(Long denemeId) {
        DenemeSinavi ds = denemeRepo.findById(denemeId)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + denemeId));
        
        return soruRepo.findByDenemeSinaviOrderBySoruNoAsc(ds).stream()
                .map(this::convertDenemeSoruToSoruDTO)
                .toList();
    }

    /** Deneme sınavı sorularını quiz için getir - Eski format (geriye dönük uyumluluk için) */
    @Transactional(readOnly = true)
    public List<DenemeSinaviSoruDTOForQuiz> getSorularForQuiz(Long denemeId) {
        DenemeSinavi ds = denemeRepo.findById(denemeId)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı bulunamadı: " + denemeId));
        
        return soruRepo.findByDenemeSinaviOrderBySoruNoAsc(ds).stream()
                .map(s -> new DenemeSinaviSoruDTOForQuiz(
                    s.getId(),
                    s.getDers() != null ? s.getDers().getId() : null,
                    s.getDers() != null ? s.getDers().getAd() : null,
                    s.getSoruNo(),
                    s.getSoruMetni(),
                    s.getSikA(),
                    s.getSikB(),
                    s.getSikC(),
                    s.getSikD(),
                    s.getSikE(),
                    s.getZorluk(),
                    s.getKonular()
                ))
                .toList();
    }

    /** Tek deneme sınavı sorusu getir (ID ile) */
    @Transactional(readOnly = true)
    public DenemeSinaviSoruDTO getSoruById(Long soruId) {
        DenemeSinaviSoru soru = soruRepo.findById(soruId)
                .orElseThrow(() -> new IllegalArgumentException("Deneme sınavı sorusu bulunamadı: " + soruId));
        return soruToDTO(soru);
    }

    // ---- DTO dönüşümleri ----
    private DenemeSinaviDTO toDTO(DenemeSinavi ds) {
        int soruSayisi = soruRepo.findByDenemeSinaviOrderBySoruNoAsc(ds).size();
        return new DenemeSinaviDTO(
            ds.getId(),
            ds.getAd(),
            ds.getTip(),
            ds.getOlusturmaTarihi() != null ? ds.getOlusturmaTarihi().toInstant() : Instant.now(),
            ds.getAciklama(),
            soruSayisi
        );
    }

    private DenemeSinaviSoruDTO soruToDTO(DenemeSinaviSoru soru) {
        return new DenemeSinaviSoruDTO(
            soru.getId(),
            soru.getDenemeSinavi().getId(),
            soru.getDers() != null ? soru.getDers().getId() : null,
            soru.getDers() != null ? soru.getDers().getAd() : null,
            soru.getSoruNo(),
            soru.getSoruMetni(),
            soru.getSikA(),
            soru.getSikB(),
            soru.getSikC(),
            soru.getSikD(),
            soru.getSikE(),
            soru.getDogruCevap(),
            soru.getZorluk(),
            soru.getKonular(),
            soru.getAciklama(),
            soru.getCozumVideosuUrl() != null ? soru.getCozumVideosuUrl() : "",
            soru.getImageUrl() != null ? soru.getImageUrl() : "",
            soru.getOlusturmaTarihi() != null ? soru.getOlusturmaTarihi().toInstant() : Instant.now()
        );
    }
}

