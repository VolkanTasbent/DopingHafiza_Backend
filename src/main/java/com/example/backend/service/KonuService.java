package com.example.backend.service;

import com.example.backend.model.Ders;
import com.example.backend.model.Konu;
import com.example.backend.repository.DersRepository;
import com.example.backend.repository.KonuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KonuService {
    private final KonuRepository konuRepo;
    private final DersRepository dersRepo;

    public KonuService(KonuRepository konuRepo, DersRepository dersRepo) {
        this.konuRepo = konuRepo; this.dersRepo = dersRepo;
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
}
