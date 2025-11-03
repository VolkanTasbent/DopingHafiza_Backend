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

    /** Deneme sınavı sorularını getir */
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
        if (req.sikA() != null) soru.setSikA(req.sikA());
        if (req.sikB() != null) soru.setSikB(req.sikB());
        if (req.sikC() != null) soru.setSikC(req.sikC());
        if (req.sikD() != null) soru.setSikD(req.sikD());
        if (req.sikE() != null) soru.setSikE(req.sikE());
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

    /** Deneme sınavı sorularını quiz için getir */
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
            soru.getOlusturmaTarihi() != null ? soru.getOlusturmaTarihi().toInstant() : Instant.now()
        );
    }
}

