package com.example.backend.service;

import com.example.backend.model.CozumKaydi;
import com.example.backend.repository.CozumKaydiRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CozumKaydiService {

    private final CozumKaydiRepository cozumRepo;

    public CozumKaydiService(CozumKaydiRepository cozumRepo) {
        this.cozumRepo = cozumRepo;
    }

    public CozumKaydi kaydet(CozumKaydi kayit) {
        return cozumRepo.save(kayit);
    }

    public List<CozumKaydi> kullaniciKayitlari(Long userId) {
        return cozumRepo.findByUserId(userId);
    }

    public Map<String, Object> konuBazliAnaliz(Long userId) {
        List<Object[]> rows = cozumRepo.getKonuBazliAnaliz(userId);
        return rows.stream().collect(Collectors.toMap(
                r -> (String) r[0],
                r -> {
                    long toplam = (long) r[1];
                    long dogru = (long) r[2];
                    double yuzde = toplam > 0 ? (dogru * 100.0 / toplam) : 0;
                    return Map.of(
                            "dogru", dogru,
                            "toplam", toplam,
                            "yuzde", yuzde
                    );
                }
        ));
    }

    public List<Object[]> gelisimVerisi(Long userId) {
        return cozumRepo.getGelisimGrafikVerisi(userId);
    }
}
