package com.example.backend.service;

import com.example.backend.dto.DailyStudyTime;
import com.example.backend.model.AppUser;
import com.example.backend.model.PomodoroSession;
import com.example.backend.model.QuizOturumu;
import com.example.backend.repository.PomodoroSessionRepository;
import com.example.backend.repository.QuizOturumuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudyTimeService {

    private final QuizOturumuRepository quizOturumuRepository;
    private final PomodoroSessionRepository pomodoroSessionRepository;

    public StudyTimeService(QuizOturumuRepository quizOturumuRepository, PomodoroSessionRepository pomodoroSessionRepository) {
        this.quizOturumuRepository = quizOturumuRepository;
        this.pomodoroSessionRepository = pomodoroSessionRepository;
    }

    @Transactional(readOnly = true)
    public List<DailyStudyTime> getDailyStudyTimes(AppUser user, Instant startDate, Instant endDate) {
        // Admin kontrolü: Admin ise tüm verileri getir, normal kullanıcı ise sadece kendi verilerini
        boolean isAdmin = user.getRole() != null && user.getRole().equals("ADMIN");
        
        List<QuizOturumu> quizOturumlari;
        List<PomodoroSession> pomodoroSessions;
        
        if (isAdmin) {
            // Admin: Tüm kullanıcıların verilerini getir
            quizOturumlari = quizOturumuRepository.findByFinishedAtBetween(startDate, endDate);
            pomodoroSessions = pomodoroSessionRepository.findByCompletedAtBetween(startDate, endDate);
        } else {
            // Normal kullanıcı: Sadece kendi verilerini getir
            quizOturumlari = quizOturumuRepository.findByUserIdAndFinishedAtBetween(user.getId(), startDate, endDate);
            pomodoroSessions = pomodoroSessionRepository.findByUserIdAndCompletedAtBetween(user.getId(), startDate, endDate);
        }
        
        // Günlere göre grupla
        Map<String, DailyStudyTime> dailyMap = new TreeMap<>(Collections.reverseOrder());
        
        // Pomodoro session'larını önce işle (başlangıç zamanlarını hesaplamak için)
        // Her pomodoro için başlangıç zamanını hesapla: completedAt - duration dakika
        Map<Long, Instant> pomodoroStartTimes = new HashMap<>();
        for (PomodoroSession session : pomodoroSessions) {
            String date = formatDate(session.getCompletedAt());
            DailyStudyTime daily = dailyMap.computeIfAbsent(date, k -> {
                DailyStudyTime d = new DailyStudyTime();
                d.setDate(date);
                d.setSoruCozmeMinutes(0);
                d.setPomodoroMinutes(0);
                d.setSoruCozmeSessions(0);
                d.setPomodoroSessions(0);
                return d;
            });
            
            // Pomodoro süresini ekle
            int pomodoroDuration = session.getDuration() != null ? session.getDuration() : 0;
            daily.setPomodoroMinutes(daily.getPomodoroMinutes() + pomodoroDuration);
            daily.setPomodoroSessions(daily.getPomodoroSessions() + 1);
            
            // Pomodoro başlangıç zamanını hesapla (completedAt - duration dakika)
            Instant pomodoroStart = session.getCompletedAt().minusSeconds(pomodoroDuration * 60L);
            pomodoroStartTimes.put(session.getId(), pomodoroStart);
        }
        
        // Quiz oturumlarını işle (pomodoro sırasında çözülen soruları çıkar)
        for (QuizOturumu oturum : quizOturumlari) {
            if (oturum.getFinishedAt() == null || oturum.getStartedAt() == null) continue;
            
            // Bu quiz oturumu pomodoro sırasında mı çözüldü?
            boolean isDuringPomodoro = false;
            Long userId = null;
            try {
                userId = oturum.getUser() != null ? oturum.getUser().getId() : null;
            } catch (Exception e) {
                // LAZY fetch hatası olabilir, userId'yi null bırak
            }
            
            for (PomodoroSession session : pomodoroSessions) {
                // Aynı kullanıcı mı? (admin durumunda tüm kullanıcılar için kontrol et)
                if (userId != null && !session.getUserId().equals(userId)) continue;
                
                // Pomodoro başlangıç zamanını al
                Instant pomodoroStart = pomodoroStartTimes.get(session.getId());
                if (pomodoroStart == null) continue;
                Instant pomodoroEnd = session.getCompletedAt();
                
                // Quiz oturumu pomodoro zamanı içinde mi?
                // Quiz'in başlangıcı veya bitişi pomodoro zamanı içindeyse, pomodoro sırasında çözülmüş demektir
                // Veya quiz'in tamamı pomodoro zamanı içindeyse
                boolean quizStartsInPomodoro = (oturum.getStartedAt().isAfter(pomodoroStart) || oturum.getStartedAt().equals(pomodoroStart)) &&
                                               (oturum.getStartedAt().isBefore(pomodoroEnd) || oturum.getStartedAt().equals(pomodoroEnd));
                boolean quizEndsInPomodoro = (oturum.getFinishedAt().isAfter(pomodoroStart) || oturum.getFinishedAt().equals(pomodoroStart)) &&
                                            (oturum.getFinishedAt().isBefore(pomodoroEnd) || oturum.getFinishedAt().equals(pomodoroEnd));
                boolean quizOverlapsPomodoro = oturum.getStartedAt().isBefore(pomodoroEnd) && oturum.getFinishedAt().isAfter(pomodoroStart);
                
                if (quizStartsInPomodoro || quizEndsInPomodoro || quizOverlapsPomodoro) {
                    isDuringPomodoro = true;
                    break;
                }
            }
            
            // Eğer pomodoro sırasında çözülmediyse, süreyi ekle
            if (!isDuringPomodoro) {
                String date = formatDate(oturum.getFinishedAt());
                DailyStudyTime daily = dailyMap.computeIfAbsent(date, k -> {
                    DailyStudyTime d = new DailyStudyTime();
                    d.setDate(date);
                    d.setSoruCozmeMinutes(0);
                    d.setPomodoroMinutes(0);
                    d.setSoruCozmeSessions(0);
                    d.setPomodoroSessions(0);
                    return d;
                });
                
                // Süreyi dakikaya çevir (duration_ms / 60000)
                long minutes = oturum.getDurationMs() != null ? oturum.getDurationMs() / 60000 : 0;
                daily.setSoruCozmeMinutes(daily.getSoruCozmeMinutes() + (int) minutes);
                daily.setSoruCozmeSessions(daily.getSoruCozmeSessions() + 1);
            }
        }
        
        // Toplam dakikaları hesapla ve formatla
        List<DailyStudyTime> result = new ArrayList<>();
        for (DailyStudyTime daily : dailyMap.values()) {
            int totalMinutes = (daily.getSoruCozmeMinutes() != null ? daily.getSoruCozmeMinutes() : 0) +
                              (daily.getPomodoroMinutes() != null ? daily.getPomodoroMinutes() : 0);
            daily.setTotalMinutes(totalMinutes);
            daily.setHours(totalMinutes / 60);
            daily.setMinutes(totalMinutes % 60);
            result.add(daily);
        }
        
        return result;
    }
    
    private String formatDate(Instant instant) {
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}

