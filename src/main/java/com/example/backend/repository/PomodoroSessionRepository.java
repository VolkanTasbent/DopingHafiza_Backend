package com.example.backend.repository;

import com.example.backend.model.PomodoroSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
    List<PomodoroSession> findByUserId(Long userId);
    
    List<PomodoroSession> findByUserIdAndCompletedAtAfter(Long userId, Instant after);
}

