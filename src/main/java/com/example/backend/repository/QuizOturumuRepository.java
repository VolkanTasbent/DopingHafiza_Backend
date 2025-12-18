// src/main/java/com/example/backend/repository/QuizOturumuRepository.java
package com.example.backend.repository;

import com.example.backend.model.AppUser;
import com.example.backend.model.QuizOturumu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuizOturumuRepository extends JpaRepository<QuizOturumu, Long> {
    Page<QuizOturumu> findByUser(AppUser user, Pageable pg);
    Optional<QuizOturumu> findByIdAndUser(Long id, AppUser user);
    
    // Günlük performans grafiği için sorgular
    @Query("SELECT q FROM QuizOturumu q WHERE q.user.id = :userId AND q.finishedAt >= :startDate AND q.finishedAt <= :endDate AND q.finishedAt IS NOT NULL")
    List<QuizOturumu> findByUserIdAndFinishedAtBetween(@Param("userId") Long userId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
    
    @Query(value = "SELECT * FROM quiz_oturumu q WHERE q.user_id = :userId AND DATE(q.finished_at) = DATE(:date) AND q.finished_at IS NOT NULL", nativeQuery = true)
    List<QuizOturumu> findByUserIdAndFinishedAtDate(@Param("userId") Long userId, @Param("date") Instant date);
    
    // Admin için tüm quiz oturumlarını getir (tarih aralığı ile)
    @Query("SELECT q FROM QuizOturumu q WHERE q.finishedAt >= :startDate AND q.finishedAt <= :endDate AND q.finishedAt IS NOT NULL")
    List<QuizOturumu> findByFinishedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}
