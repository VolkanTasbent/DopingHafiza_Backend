package com.example.backend.repository;

import com.example.backend.model.PomodoroSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
    List<PomodoroSession> findByUserId(Long userId);
    
    List<PomodoroSession> findByUserIdAndCompletedAtAfter(Long userId, Instant after);
    
    // Günlük performans grafiği için sorgular
    @Query("SELECT p FROM PomodoroSession p WHERE p.userId = :userId AND p.completedAt >= :startDate AND p.completedAt <= :endDate")
    List<PomodoroSession> findByUserIdAndCompletedAtBetween(@Param("userId") Long userId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
    
    @Query(value = "SELECT * FROM pomodoro_session p WHERE p.user_id = :userId AND DATE(p.completed_at) = DATE(:date)", nativeQuery = true)
    List<PomodoroSession> findByUserIdAndCompletedAtDate(@Param("userId") Long userId, @Param("date") Instant date);
    
    // Admin için tüm pomodoro session'larını getir (tarih aralığı ile)
    @Query("SELECT p FROM PomodoroSession p WHERE p.completedAt >= :startDate AND p.completedAt <= :endDate")
    List<PomodoroSession> findByCompletedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}


