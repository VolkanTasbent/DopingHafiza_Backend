package com.example.backend.repository;

import com.example.backend.model.VideoNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoNoteRepository extends JpaRepository<VideoNote, Long> {
    
    List<VideoNote> findByUserId(Long userId);
    
    List<VideoNote> findByUserIdAndKonuId(Long userId, Long konuId);
    
    List<VideoNote> findByUserIdAndVideoUrl(Long userId, String videoUrl);
    
    // YENİ: videoId'ye göre filtreleme metodları
    List<VideoNote> findByKonuIdAndVideoIdAndUserId(Long konuId, String videoId, Long userId);
    
    List<VideoNote> findByKonuIdAndVideoIdAndUserIdOrderByTimestampSecondsAsc(Long konuId, String videoId, Long userId);
    
    // videoUrl'e göre filtreleme (geriye dönük uyumluluk)
    List<VideoNote> findByKonuIdAndVideoUrlAndUserId(Long konuId, String videoUrl, Long userId);
    
    List<VideoNote> findByKonuIdAndVideoUrlAndUserIdOrderByTimestampSecondsAsc(Long konuId, String videoUrl, Long userId);
    
    List<VideoNote> findByUserIdOrderByTimestampSecondsAsc(Long userId);
    
    List<VideoNote> findByUserIdAndKonuIdOrderByTimestampSecondsAsc(Long userId, Long konuId);
    
    List<VideoNote> findByUserIdAndVideoUrlOrderByTimestampSecondsAsc(Long userId, String videoUrl);
    
    // Esnek filtreleme: videoId varsa videoId ile, yoksa videoUrl ile
    // videoId null olan kayıtları da dahil et (eski notlar için)
    @Query("SELECT n FROM VideoNote n WHERE n.konuId = :konuId AND n.userId = :userId " +
           "AND ((:videoId IS NOT NULL AND :videoId != '' AND (:videoUrl IS NULL OR :videoId != :videoUrl) AND n.videoId = :videoId) " +
           "OR (:videoUrl IS NOT NULL AND :videoUrl != '' AND n.videoUrl = :videoUrl AND (n.videoId IS NULL OR (:videoId IS NOT NULL AND n.videoId = :videoId)))) " +
           "ORDER BY n.timestampSeconds ASC")
    List<VideoNote> findByKonuIdAndUserIdWithFlexibleVideoFilter(
        @Param("konuId") Long konuId,
        @Param("userId") Long userId,
        @Param("videoId") String videoId,
        @Param("videoUrl") String videoUrl
    );
}






