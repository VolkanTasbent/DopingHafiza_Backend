package com.example.backend.service;

import com.example.backend.dto.PomodoroPeriodStats;
import com.example.backend.dto.PomodoroSessionRequest;
import com.example.backend.dto.PomodoroSessionResponse;
import com.example.backend.dto.PomodoroStatsResponse;
import com.example.backend.model.PomodoroSession;
import com.example.backend.repository.PomodoroSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PomodoroService {
    private final PomodoroSessionRepository repository;

    public PomodoroService(PomodoroSessionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PomodoroSessionResponse saveSession(Long userId, PomodoroSessionRequest request) {
        PomodoroSession session = new PomodoroSession();
        session.setUserId(userId);
        session.setDuration(request.getDuration());
        
        // completedAt belirtilmişse parse et, yoksa şu anki zamanı kullan
        if (request.getCompletedAt() != null && !request.getCompletedAt().isBlank()) {
            session.setCompletedAt(Instant.parse(request.getCompletedAt()));
        } else {
            session.setCompletedAt(Instant.now());
        }
        
        session.setCreatedAt(Instant.now());
        
        PomodoroSession saved = repository.save(session);
        return PomodoroSessionResponse.from(saved);
    }

    public PomodoroStatsResponse getStats(Long userId) {
        Instant now = Instant.now();
        ZoneId zoneId = ZoneId.systemDefault();
        
        // Bugünün başlangıcı
        Instant todayStart = now.atZone(zoneId)
                .toLocalDate()
                .atStartOfDay(zoneId)
                .toInstant();
        
        // Bu haftanın başlangıcı (7 gün önce)
        Instant weekStart = todayStart.minus(7, ChronoUnit.DAYS);
        
        // Bu ayın başlangıcı (30 gün önce)
        Instant monthStart = todayStart.minus(30, ChronoUnit.DAYS);
        
        // Bugün
        List<PomodoroSession> todaySessions = repository.findByUserIdAndCompletedAtAfter(userId, todayStart);
        int todayCount = todaySessions.size();
        int todayMinutes = todaySessions.stream()
                .mapToInt(PomodoroSession::getDuration)
                .sum();
        
        // Bu Hafta
        List<PomodoroSession> weekSessions = repository.findByUserIdAndCompletedAtAfter(userId, weekStart);
        int weekCount = weekSessions.size();
        int weekMinutes = weekSessions.stream()
                .mapToInt(PomodoroSession::getDuration)
                .sum();
        
        // Bu Ay
        List<PomodoroSession> monthSessions = repository.findByUserIdAndCompletedAtAfter(userId, monthStart);
        int monthCount = monthSessions.size();
        int monthMinutes = monthSessions.stream()
                .mapToInt(PomodoroSession::getDuration)
                .sum();
        
        // Toplam
        List<PomodoroSession> allSessions = repository.findByUserId(userId);
        int totalCount = allSessions.size();
        int totalMinutes = allSessions.stream()
                .mapToInt(PomodoroSession::getDuration)
                .sum();
        
        PomodoroStatsResponse stats = new PomodoroStatsResponse();
        stats.setToday(new PomodoroPeriodStats(todayCount, todayMinutes));
        stats.setWeek(new PomodoroPeriodStats(weekCount, weekMinutes));
        stats.setMonth(new PomodoroPeriodStats(monthCount, monthMinutes));
        stats.setTotal(new PomodoroPeriodStats(totalCount, totalMinutes));
        
        return stats;
    }
}

