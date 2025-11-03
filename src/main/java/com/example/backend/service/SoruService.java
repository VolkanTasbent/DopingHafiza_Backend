package com.example.backend.service;

import com.example.backend.dto.KonuDTO;
import com.example.backend.dto.SecenekDTO;
import com.example.backend.dto.SoruDTO;
import com.example.backend.model.Ders;
import com.example.backend.model.Konu;
import com.example.backend.model.Secenek;
import com.example.backend.model.Soru;
import com.example.backend.repository.DersRepository;
import com.example.backend.repository.KonuRepository;
import com.example.backend.repository.SecenekRepository;
import com.example.backend.repository.SoruRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class SoruService {

    private final DersRepository dersRepo;
    private final KonuRepository konuRepo;
    private final SoruRepository soruRepo;
    private final SecenekRepository secenekRepo;

    public SoruService(DersRepository dersRepo,
                       KonuRepository konuRepo,
                       SoruRepository soruRepo,
                       SecenekRepository secenekRepo) {
        this.dersRepo = dersRepo;
        this.konuRepo = konuRepo;
        this.soruRepo = soruRepo;
        this.secenekRepo = secenekRepo;
    }

    /** Ders bazlı listeleme (limit ile) */
    @Transactional(readOnly = true)
    public List<SoruDTO> getSorular(Long dersId, Integer limit) {
        Ders ders = dersRepo.findById(dersId)
                .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
        int lim = (limit != null && limit > 0) ? limit : 50;
        var list = soruRepo.findByDersOrderByIdAsc(ders, PageRequest.of(0, lim));
        return list.stream().map(this::toDTO).toList();
    }

    /** Ders + Konu bazlı listeleme (limit ile) */
    @Transactional(readOnly = true)
    public List<SoruDTO> getSorularByKonu(Long dersId, Long konuId, Integer limit) {
        Ders ders = dersRepo.findById(dersId)
                .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
        int lim = (limit != null && limit > 0) ? limit : 50;
        var list = soruRepo.findDistinctByDersAndKonular_IdOrderByIdAsc(ders, konuId, PageRequest.of(0, lim));
        return list.stream().map(this::toDTO).toList();
    }

    /** Ders bazlı sayfalı listeleme */
    @Transactional(readOnly = true)
    public List<SoruDTO> getSorularPaged(Long dersId, int page, int size) {
        Ders ders = dersRepo.findById(dersId)
                .orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
        var list = soruRepo.findByDersOrderByIdAsc(ders, PageRequest.of(Math.max(0, page), Math.max(1, size)));
        return list.stream().map(this::toDTO).toList();
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
        return new SecenekDTO(o.getId(), o.getMetin(), o.isDogru(), o.getSiralama());
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
        return new SecenekDTO(o.getId(), o.getMetin(), o.isDogru(), o.getSiralama());
    }

    /** Soru sil */
    @Transactional
    public void deleteSoru(Long id) {
        if (!soruRepo.existsById(id)) {
            throw new IllegalArgumentException("Soru bulunamadı: " + id);
        }
        soruRepo.deleteById(id);
    }

    // ---- Dönüşüm ----
    private SoruDTO toDTO(Soru s) {
        var konuDtos = s.getKonular().stream()
                .map(k -> new KonuDTO(k.getId(), k.getAd(), k.getDokumanUrl(), k.getDokumanAdi(), k.getKonuAnlatimVideosuUrl()))
                .toList();

        var opts = secenekRepo.findBySoruOrderBySiralamaAscIdAsc(s).stream()
                .map(o -> new SecenekDTO(o.getId(), o.getMetin(), o.isDogru(), o.getSiralama()))
                .toList();

        return new SoruDTO(
                s.getId(),
                s.getMetin(),
                s.getTip(),
                s.getZorluk(),
                s.getImageUrl(),
                s.getDers().getAd(),
                konuDtos,
                opts,
                s.getCozumVideosuUrl()
        );
    }
}
