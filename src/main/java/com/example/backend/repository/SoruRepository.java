package com.example.backend.repository;

import com.example.backend.model.Ders;
import com.example.backend.model.Soru;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SoruRepository extends JpaRepository<Soru, Long> {

    @EntityGraph(attributePaths = {"ders", "konular"})
    List<Soru> findByDersOrderByIdAsc(Ders ders, Pageable pageable);

    @EntityGraph(attributePaths = {"ders", "konular"})
    List<Soru> findDistinctByDersAndKonular_IdOrderByIdAsc(Ders ders, Long konuId, Pageable pageable);

    @EntityGraph(attributePaths = {"ders", "konular"})
    Optional<Soru> findWithRelsById(Long id);

    @Query("select max(s.soruNo) from Soru s where s.ders = :ders")
    Integer findMaxSoruNoByDers(@Param("ders") Ders ders);

    // 🔥 FLASHCARD İÇİN — Derse göre RASTGELE 20 soru
    // FlashCard için rastgele soru getiren metod
    // FlashCard için rastgele soru getiren metod - DÜZELTİLMİŞ VERSİYON
    @Query("""
    SELECT s FROM Soru s
    LEFT JOIN FETCH s.secenekler
    WHERE s.ders.id = :dersId
    ORDER BY RANDOM()
    """)
    List<Soru> findRandomQuestionsWithSecenek(@Param("dersId") Long dersId);

    // Konuya bağlı soru sayısını döndürür
    @Query("SELECT COUNT(DISTINCT s.id) FROM Soru s JOIN s.konular k WHERE k.id = :konuId")
    long countByKonularId(@Param("konuId") Long konuId);

}
