package com.example.backend.repository;
import com.example.backend.model.Cevap;
import com.example.backend.model.QuizOturumu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
public interface CevapRepository extends JpaRepository<Cevap, Long> {
    List<Cevap> findByOturum(QuizOturumu o);

    @Query(value = """
        SELECT
          d.id AS ders_id,
          d.ad AS ders_ad,
          k.id AS konu_id,
          k.ad AS konu_ad,
          COUNT(*) AS total_count,
          SUM(CASE WHEN c.dogru = TRUE THEN 1 ELSE 0 END) AS correct_count,
          SUM(CASE WHEN c.dogru = FALSE THEN 1 ELSE 0 END) AS wrong_count,
          SUM(CASE WHEN c.secenek_id IS NULL THEN 1 ELSE 0 END) AS blank_count
        FROM cevap c
        JOIN quiz_oturumu qo ON qo.id = c.oturum_id
        JOIN soru s ON s.id = c.soru_id
        JOIN ders d ON d.id = s.ders_id
        JOIN soru_konu sk ON sk.soru_id = s.id
        JOIN konu k ON k.id = sk.konu_id
        WHERE qo.user_id = :userId
          AND qo.finished_at IS NOT NULL
          AND qo.finished_at >= :fromEpochMs
        GROUP BY d.id, d.ad, k.id, k.ad
        ORDER BY d.ad, k.ad
        """, nativeQuery = true)
    List<Object[]> findTopicPerformanceForUser(
            @Param("userId") Long userId,
            @Param("fromEpochMs") Instant fromEpochMs
    );

    @Query(value = """
        SELECT
          qo.user_id AS user_id,
          d.id AS ders_id,
          d.ad AS ders_ad,
          k.id AS konu_id,
          k.ad AS konu_ad,
          COUNT(*) AS total_count,
          SUM(CASE WHEN c.dogru = TRUE THEN 1 ELSE 0 END) AS correct_count,
          SUM(CASE WHEN c.dogru = FALSE THEN 1 ELSE 0 END) AS wrong_count,
          SUM(CASE WHEN c.secenek_id IS NULL THEN 1 ELSE 0 END) AS blank_count
        FROM cevap c
        JOIN quiz_oturumu qo ON qo.id = c.oturum_id
        JOIN soru s ON s.id = c.soru_id
        JOIN ders d ON d.id = s.ders_id
        JOIN soru_konu sk ON sk.soru_id = s.id
        JOIN konu k ON k.id = sk.konu_id
        WHERE qo.finished_at IS NOT NULL
          AND qo.finished_at >= :fromEpochMs
        GROUP BY qo.user_id, d.id, d.ad, k.id, k.ad
        HAVING COUNT(*) >= :minAnswers
        ORDER BY qo.user_id, d.ad, k.ad
        """, nativeQuery = true)
    List<Object[]> findTrainingRows(
            @Param("fromEpochMs") Instant fromEpochMs,
            @Param("minAnswers") Integer minAnswers
    );
}
