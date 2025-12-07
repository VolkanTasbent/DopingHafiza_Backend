package com.example.backend.repository;

import com.example.backend.model.VideoNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoNoteRepository extends JpaRepository<VideoNote, Long> {
    
    List<VideoNote> findByUserId(Long userId);
    
    List<VideoNote> findByUserIdAndKonuId(Long userId, Long konuId);
    
    List<VideoNote> findByUserIdAndVideoUrl(Long userId, String videoUrl);
    
    List<VideoNote> findByUserIdOrderByTimestampSecondsAsc(Long userId);
    
    List<VideoNote> findByUserIdAndKonuIdOrderByTimestampSecondsAsc(Long userId, Long konuId);
    
    List<VideoNote> findByUserIdAndVideoUrlOrderByTimestampSecondsAsc(Long userId, String videoUrl);
}

