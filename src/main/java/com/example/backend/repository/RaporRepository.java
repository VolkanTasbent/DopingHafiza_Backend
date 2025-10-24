// src/main/java/com/example/backend/repository/RaporRepository.java
package com.example.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.model.Cevap;

public interface RaporRepository extends JpaRepository<Cevap, Long> {

    /**
     * Verilen oturum için (opsiyonel) sadece yanlışları getiren sorgu.
     * NOT: Tablolar: cevap, soru, soru_konu, konu, ders, secenek
     */
    @Query(value = """
        SELECT 
          s.id                        AS soru_id,
          s.metin                     AS soru_metin,
          k.ad                        AS konu_ad,
          d.ad                        AS ders_ad,
          sec.metin                   AS secilen_secenek,
          dogru_sec.metin             AS dogru_secenek,
          c.dogru                     AS dogru_mu
        FROM cevap c
        JOIN soru s          ON s.id = c.soru_id
        JOIN soru_konu sk    ON sk.soru_id = s.id
        JOIN konu k          ON k.id = sk.konu_id
        JOIN ders d          ON d.id = k.ders_id
        LEFT JOIN secenek sec       ON sec.id = c.secenek_id
        JOIN secenek dogru_sec      ON dogru_sec.soru_id = s.id AND dogru_sec.dogru = TRUE
        WHERE c.oturum_id = :oturumId
          AND (:sadeceYanlis = TRUE  AND c.dogru = FALSE  OR :sadeceYanlis = FALSE)
        ORDER BY s.id
        """, nativeQuery = true)
    List<Object[]> findOturumDetay(@Param("oturumId") Long oturumId,
                                   @Param("sadeceYanlis") boolean sadeceYanlis);
}
