package com.example.backend.service;

import com.example.backend.dto.KonuDTO;
import com.example.backend.dto.KonuUpdateDTO;
import com.example.backend.dto.KonuVideoDTO;
import com.example.backend.exception.ConflictException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Ders;
import com.example.backend.model.Konu;
import com.example.backend.model.KonuVideo;
import com.example.backend.repository.DersRepository;
import com.example.backend.repository.KonuRepository;
import com.example.backend.repository.KonuVideoRepository;
import com.example.backend.repository.SoruRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KonuService {
    private final KonuRepository konuRepo;
    private final DersRepository dersRepo;
    private final SoruRepository soruRepo;
    private final KonuVideoRepository konuVideoRepo;

    public KonuService(KonuRepository konuRepo, DersRepository dersRepo, SoruRepository soruRepo, KonuVideoRepository konuVideoRepo) {
        this.konuRepo = konuRepo; 
        this.dersRepo = dersRepo;
        this.soruRepo = soruRepo;
        this.konuVideoRepo = konuVideoRepo;
    }

    public List<Konu> listByDers(Long dersId) {
        Ders d = dersRepo.findById(dersId).orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
        return konuRepo.findByDersOrderByAdAsc(d);
    }

    @Transactional
    public Konu create(Long dersId, String ad) {
        if (ad == null || ad.isBlank()) throw new IllegalArgumentException("Konu adı boş olamaz");
        Ders d = dersRepo.findById(dersId).orElseThrow(() -> new IllegalArgumentException("Ders bulunamadı: " + dersId));
        Konu k = new Konu();
        k.setDers(d);
        k.setAd(ad.trim());
        return konuRepo.save(k);
    }

    /**
     * Konu bilgilerini günceller (Partial Update - Sadece gönderilen alanlar güncellenir)
     */
    @Transactional
    public Konu updateKonu(Long konuId, KonuUpdateDTO updateDTO) {
        // Ders ile birlikte fetch et (lazy loading sorununu önlemek için)
        Konu konu = konuRepo.findWithDersById(konuId)
            .orElseThrow(() -> new ResourceNotFoundException("Konu bulunamadı: " + konuId));
        
        // ✅ Partial Update: Sadece gönderilen (null olmayan) alanları güncelle
        
        // Ad güncellemesi (boş olamaz, sadece gönderilmişse güncelle)
        if (updateDTO.getAd() != null) {
            String trimmedAd = updateDTO.getAd().trim();
            if (!trimmedAd.isEmpty()) {
                konu.setAd(trimmedAd);
            }
            // Eğer boş string gönderilirse, ad değiştirilmez (mevcut ad korunur)
        }
        
        // Açıklama güncellemesi (opsiyonel, sadece gönderilmişse güncelle)
        if (updateDTO.getAciklama() != null) {
            String trimmedAciklama = updateDTO.getAciklama().trim();
            // Boş string gönderilirse null yap (silme işlemi)
            konu.setAciklama(trimmedAciklama.isEmpty() ? null : trimmedAciklama);
        }
        // Eğer aciklama gönderilmemişse (null), mevcut değer korunur
        
        // ✅ Video URL güncellemesi (opsiyonel, sadece gönderilmişse güncelle)
        if (updateDTO.getKonuAnlatimVideosuUrl() != null) {
            String trimmedUrl = updateDTO.getKonuAnlatimVideosuUrl().trim();
            // Boş string gönderilirse null yap (silme işlemi)
            String finalUrl = trimmedUrl.isEmpty() ? null : trimmedUrl;
            konu.setKonuAnlatimVideosuUrl(finalUrl);
            System.out.println("✅ Video URL güncelleniyor - Konu ID: " + konuId + ", URL: " + finalUrl);
        } else {
            System.out.println("ℹ️ Video URL gönderilmedi, mevcut değer korunuyor - Konu ID: " + konuId);
        }
        // Eğer video URL gönderilmemişse (null), mevcut değer korunur
        
        // Döküman URL güncellemesi (opsiyonel, sadece gönderilmişse güncelle)
        if (updateDTO.getDokumanUrl() != null) {
            String trimmedUrl = updateDTO.getDokumanUrl().trim();
            // Boş string gönderilirse null yap (silme işlemi)
            konu.setDokumanUrl(trimmedUrl.isEmpty() ? null : trimmedUrl);
        }
        // Eğer döküman URL gönderilmemişse (null), mevcut değer korunur
        
        Konu saved = konuRepo.save(konu);
        System.out.println("✅ Konu kaydedildi - ID: " + saved.getId() + ", Video URL: " + saved.getKonuAnlatimVideosuUrl());
        return saved;
    }

    /**
     * Konuya video ekler (dosya veya URL)
     */
    @Transactional
    public KonuVideo addVideo(Long konuId, String videoUrl, String videoAdi) {
        Konu konu = konuRepo.findById(konuId)
            .orElseThrow(() -> new ResourceNotFoundException("Konu bulunamadı: " + konuId));
        
        // Mevcut video sayısını al (sıralama için)
        List<KonuVideo> mevcutVideolar = konuVideoRepo.findByKonuIdOrderBySiralamaAsc(konuId);
        int yeniSiralama = mevcutVideolar.size();
        
        KonuVideo video = new KonuVideo();
        video.setKonu(konu);
        video.setVideoUrl(videoUrl);
        video.setVideoAdi(videoAdi);
        video.setSiralama(yeniSiralama);
        
        return konuVideoRepo.save(video);
    }

    /**
     * Konudan video siler
     */
    @Transactional
    public void deleteVideo(Long videoId) {
        KonuVideo video = konuVideoRepo.findById(videoId)
            .orElseThrow(() -> new ResourceNotFoundException("Video bulunamadı: " + videoId));
        konuVideoRepo.delete(video);
    }

    /**
     * KonuDTO'yu oluşturur (videolar dahil)
     */
    public KonuDTO toDTO(Konu konu) {
        Long dersId = konu.getDers() != null ? konu.getDers().getId() : null;
        
        // Videoları getir ve DTO'ya çevir
        List<KonuVideoDTO> videoDTOs = konuVideoRepo.findByKonuIdOrderBySiralamaAsc(konu.getId())
            .stream()
            .map(v -> new KonuVideoDTO(v.getId(), v.getVideoUrl(), v.getVideoAdi(), v.getSiralama()))
            .collect(Collectors.toList());
        
        return new KonuDTO(
            konu.getId(),
            konu.getAd(),
            konu.getDokumanUrl(),
            konu.getDokumanAdi(),
            konu.getKonuAnlatimVideosuUrl(),
            konu.getAciklama(),
            dersId,
            videoDTOs
        );
    }

    /**
     * Konuyu siler (eğer konuya bağlı soru yoksa)
     */
    @Transactional
    public void deleteKonu(Long konuId) {
        Konu konu = konuRepo.findById(konuId)
            .orElseThrow(() -> new ResourceNotFoundException("Konu bulunamadı: " + konuId));
        
        // Konuya bağlı soru var mı kontrol et
        long soruCount = soruRepo.countByKonularId(konuId);
        if (soruCount > 0) {
            throw new ConflictException("Bu konuya bağlı " + soruCount + " soru bulunmaktadır. Önce soruları silin veya başka konuya taşıyın.");
        }
        
        konuRepo.delete(konu);
    }
}
