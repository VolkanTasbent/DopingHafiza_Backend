package com.example.backend.repository;

import com.example.backend.model.CozumKaydi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CozumKaydiRepository extends JpaRepository<CozumKaydi, Long> {

    List<CozumKaydi> findByUserId(Long userId);

    List<CozumKaydi> findByUserIdAndKonuId(Long userId, Long konuId);

    @Query("SELECT c.konu.ad, COUNT(c), SUM(CASE WHEN c.dogruMu = true THEN 1 ELSE 0 END) " +
            "FROM CozumKaydi c WHERE c.user.id = :userId GROUP BY c.konu.ad")
    List<Object[]> getKonuBazliAnaliz(Long userId);

    @Query("SELECT c.tarih, COUNT(c), SUM(CASE WHEN c.dogruMu = true THEN 1 ELSE 0 END) " +
            "FROM CozumKaydi c WHERE c.user.id = :userId GROUP BY c.tarih ORDER BY c.tarih")
    List<Object[]> getGelisimGrafikVerisi(Long userId);
}
