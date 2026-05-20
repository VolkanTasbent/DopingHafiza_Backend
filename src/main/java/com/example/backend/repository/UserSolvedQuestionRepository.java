package com.example.backend.repository;

import com.example.backend.model.UserSolvedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserSolvedQuestionRepository extends JpaRepository<UserSolvedQuestion, Long> {

    Optional<UserSolvedQuestion> findByUserIdAndSoru_Id(Long userId, Long soruId);

    /** Dogru veya yanlis — listeden atlanacak */
    @Query("""
        SELECT usq.soru.id FROM UserSolvedQuestion usq
        WHERE usq.userId = :userId AND usq.sonDurum IN ('DOGRU', 'YANLIS')
        """)
    Set<Long> findExcludedSoruIdsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT usq.soru.id FROM UserSolvedQuestion usq
        WHERE usq.userId = :userId AND usq.sonDurum IN ('DOGRU', 'YANLIS') AND usq.soru.ders.id = :dersId
        """)
    Set<Long> findExcludedSoruIdsByUserIdAndDersId(@Param("userId") Long userId, @Param("dersId") Long dersId);

    @Query("""
        SELECT usq.soru.id FROM UserSolvedQuestion usq
        JOIN usq.soru.konular k
        WHERE usq.userId = :userId AND usq.sonDurum IN ('DOGRU', 'YANLIS') AND k.id = :konuId
        """)
    Set<Long> findExcludedSoruIdsByUserIdAndKonuId(@Param("userId") Long userId, @Param("konuId") Long konuId);

    /** Once bos birakilmis — tekrar gelir, etiket icin */
    @Query("""
        SELECT usq.soru.id FROM UserSolvedQuestion usq
        WHERE usq.userId = :userId AND usq.sonDurum = 'BOS'
        """)
    Set<Long> findBlankSoruIdsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT usq.soru.id FROM UserSolvedQuestion usq
        WHERE usq.userId = :userId AND usq.sonDurum = 'BOS' AND usq.soru.ders.id = :dersId
        """)
    Set<Long> findBlankSoruIdsByUserIdAndDersId(@Param("userId") Long userId, @Param("dersId") Long dersId);

    @Query("""
        SELECT usq.soru.id FROM UserSolvedQuestion usq
        JOIN usq.soru.konular k
        WHERE usq.userId = :userId AND usq.sonDurum = 'BOS' AND k.id = :konuId
        """)
    Set<Long> findBlankSoruIdsByUserIdAndKonuId(@Param("userId") Long userId, @Param("konuId") Long konuId);

    List<UserSolvedQuestion> findByUserIdOrderByCozulduAtDesc(Long userId);

    @Query("""
        SELECT COUNT(usq) FROM UserSolvedQuestion usq
        WHERE usq.userId = :userId AND usq.sonDurum IN ('DOGRU', 'YANLIS')
        """)
    long countAnsweredByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(usq) FROM UserSolvedQuestion usq
        WHERE usq.userId = :userId AND usq.sonDurum IN ('DOGRU', 'YANLIS') AND usq.soru.ders.id = :dersId
        """)
    long countAnsweredByUserIdAndDersId(@Param("userId") Long userId, @Param("dersId") Long dersId);

    @Query("""
        SELECT COUNT(DISTINCT usq.soru.id) FROM UserSolvedQuestion usq
        JOIN usq.soru.konular k
        WHERE usq.userId = :userId AND usq.sonDurum IN ('DOGRU', 'YANLIS') AND k.id = :konuId
        """)
    long countAnsweredByUserIdAndKonuId(@Param("userId") Long userId, @Param("konuId") Long konuId);
}
