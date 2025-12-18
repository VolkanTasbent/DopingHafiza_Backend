package com.example.backend.service;

import com.example.backend.dto.DailyPomodoroStat;
import com.example.backend.dto.DailyPomodoroStatsResponse;
import com.example.backend.dto.PomodoroPeriodStats;
import com.example.backend.dto.PomodoroSessionRequest;
import com.example.backend.dto.PomodoroSessionResponse;
import com.example.backend.dto.PomodoroStatsResponse;
import com.example.backend.model.PomodoroSession;
import com.example.backend.model.UserActivity;
import com.example.backend.repository.PomodoroSessionRepository;
import com.example.backend.repository.UserActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PomodoroService {
    private final PomodoroSessionRepository repository;
    private final UserActivityRepository userActivityRepository;

    public PomodoroService(PomodoroSessionRepository repository, UserActivityRepository userActivityRepository) {
        this.repository = repository;
        this.userActivityRepository = userActivityRepository;
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
        
        // Aktivite kaydet
        try {
            UserActivity activity = new UserActivity();
            activity.setUserId(userId);
            activity.setActivityType("pomodoro");
            activity.setActivityTitle("Pomodoro Çalışması");
            activity.setActivitySubtitle(request.getDuration() + " dakika çalışma");
            activity.setActivityIcon("grid");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("duration", request.getDuration());
            activity.setMetadata(metadata);
            
            userActivityRepository.save(activity);
            System.out.println("✅ Pomodoro aktivitesi kaydedildi: " + request.getDuration() + " dakika");
        } catch (Exception e) {
            System.err.println("⚠️ Pomodoro aktivitesi kaydedilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        
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

    @Transactional(readOnly = true)
    public DailyPomodoroStatsResponse getDailyStats(Long userId, String startDateStr, String endDateStr) {
        // Tarihleri parse et
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        
        // Tarih aralığını Instant'a çevir
        // ÖNEMLİ: Tarihleri UTC'ye göre değil, kullanıcının local timezone'una göre hesapla
        ZoneId zoneId = ZoneId.systemDefault();
        
        // Başlangıç: günün başlangıcı (00:00:00) local timezone'da
        Instant startInstant = startDate.atStartOfDay(zoneId).toInstant();
        // Bitiş: günün sonu (23:59:59.999) local timezone'da
        Instant endInstant = endDate.atTime(23, 59, 59, 999_000_000).atZone(zoneId).toInstant();
        
        // Pomodoro session'larını getir
        List<PomodoroSession> sessions = repository.findByUserIdAndCompletedAtBetween(userId, startInstant, endInstant);
        
        // Günlük istatistikleri hesapla
        Map<String, DailyPomodoroStat> dailyStatsMap = new HashMap<>();
        
        // Tüm günleri başlat (0 ile)
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateKey = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            dailyStatsMap.put(dateKey, new DailyPomodoroStat(dateKey, 0, 0));
            currentDate = currentDate.plusDays(1);
        }
        
        // Session'ları günlere göre grupla
        // ÖNEMLİ: completedAt'ı kullanıcının local timezone'una göre tarihe çevir
        for (PomodoroSession session : sessions) {
            if (session.getCompletedAt() == null) continue;
            
            // Tarihi kullanıcının local timezone'una göre hesapla
            // Bu, pomodoro'nun tamamlandığı günü doğru şekilde belirler
            // Instant zaten UTC'de, direkt local timezone'a çevir
            LocalDate sessionDate = session.getCompletedAt().atZone(zoneId).toLocalDate();
            String dateKey = sessionDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Eğer dateKey map'te yoksa (tarih aralığı dışında), atla
            // Bu durumda session tarih aralığı dışında olabilir (timezone farkı nedeniyle)
            DailyPomodoroStat stat = dailyStatsMap.get(dateKey);
            if (stat != null) {
                stat.setCount(stat.getCount() + 1);
                stat.setMinutes(stat.getMinutes() + (session.getDuration() != null ? session.getDuration() : 0));
            } else {
                // Debug: Tarih aralığı dışında bir tarih bulundu
                System.out.println("⚠️ Pomodoro session tarihi aralık dışında: " + dateKey + 
                    " (completedAt: " + session.getCompletedAt() + 
                    ", startDate: " + startDateStr + ", endDate: " + endDateStr + ")");
            }
        }
        
        // Listeye çevir ve sırala
        List<DailyPomodoroStat> dailyStats = new ArrayList<>(dailyStatsMap.values());
        dailyStats.sort(Comparator.comparing(DailyPomodoroStat::getDate));
        
        return new DailyPomodoroStatsResponse(dailyStats);
    }
}


