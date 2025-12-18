package com.example.backend.service;

import com.example.backend.dto.ScoreBreakdown;
import com.example.backend.dto.ScoreCalculationResponse;
import com.example.backend.dto.UserStats;
import com.example.backend.model.AppUser;
import com.example.backend.model.QuizOturumu;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.QuizOturumuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class ScoreCalculationService {

    private final QuizOturumuRepository quizOturumuRepository;
    private final AppUserRepository userRepository;

    public ScoreCalculationService(QuizOturumuRepository quizOturumuRepository, AppUserRepository userRepository) {
        this.quizOturumuRepository = quizOturumuRepository;
        this.userRepository = userRepository;
    }

    /**
     * Kullanıcının puanını hesaplar ve günceller
     */
    @Transactional
    public ScoreCalculationResponse calculateAndUpdateScore(AppUser user) {
        // Kullanıcının tüm quiz oturumlarını al (finishedAt null olmayanlar)
        // Tüm oturumları almak için çok eski bir tarihten başlayalım
        Instant startDate = Instant.ofEpochMilli(0);
        Instant endDate = Instant.now();
        
        List<QuizOturumu> oturumlar = quizOturumuRepository.findByUserIdAndFinishedAtBetween(
            user.getId(),
            startDate,
            endDate
        );
        
        // Sadece tamamlanmış oturumları filtrele
        oturumlar = oturumlar.stream()
            .filter(o -> o.getFinishedAt() != null)
            .toList();

        // İstatistikleri hesapla
        int totalCorrect = oturumlar.stream()
            .mapToInt(o -> o.getCorrect() != null ? o.getCorrect() : 0)
            .sum();

        int totalWrong = oturumlar.stream()
            .mapToInt(o -> o.getWrong() != null ? o.getWrong() : 0)
            .sum();

        int totalEmpty = oturumlar.stream()
            .mapToInt(o -> o.getEmpty() != null ? o.getEmpty() : 0)
            .sum();

        // Net puan hesapla: Doğru - (Yanlış / 4)
        double totalNet = totalCorrect - (totalWrong / 4.0);

        int totalReports = oturumlar.size();

        // Doğruluk oranı
        int totalAnswered = totalCorrect + totalWrong;
        double accuracy = totalAnswered > 0 
            ? (double) totalCorrect / totalAnswered 
            : 0.0;

        // Puan hesaplama
        ScoreBreakdown breakdown = calculateScoreBreakdown(
            totalCorrect,
            totalWrong,
            totalEmpty,
            totalNet,
            totalReports,
            accuracy,
            user.getId()
        );

        int totalScore = breakdown.baseScore() + breakdown.netBonus() + 
                        breakdown.activityBonus() + breakdown.streakBonus() + 
                        breakdown.accuracyBonus();
        totalScore = Math.max(0, totalScore); // Negatif puan olmasın

        // Eski puanı al
        int oldScore = user.getPuan() != null ? user.getPuan() : 0;

        // Kullanıcı puanını güncelle
        user.setPuan(totalScore);
        userRepository.save(user);

        // Stats oluştur
        UserStats stats = new UserStats(
            totalCorrect,
            totalWrong,
            totalEmpty,
            totalNet,
            accuracy,
            totalReports
        );

        return new ScoreCalculationResponse(
            user.getId(),
            oldScore,
            totalScore,
            breakdown,
            stats
        );
    }

    /**
     * Puan hesaplama detaylarını döndürür
     */
    private ScoreBreakdown calculateScoreBreakdown(
        int totalCorrect,
        int totalWrong,
        int totalEmpty,
        double totalNet,
        int totalReports,
        double accuracy,
        Long userId
    ) {
        // 1. Temel Puan
        int baseScore = (totalCorrect * 10) - (int)(totalWrong * 2.5);

        // 2. Net Puan Bonusu
        int netBonus = (int)(totalNet * 5);

        // 3. Aktivite Bonusları
        int activityBonus = 0;
        
        // Toplam rapor sayısına göre
        if (totalReports >= 100) {
            activityBonus += 200;
        } else if (totalReports >= 50) {
            activityBonus += 100;
        } else if (totalReports >= 30) {
            activityBonus += 50;
        }

        // Günlük aktivite kontrolü (son 7 gün)
        long dailyActivity = getDailyActivityCount(userId, 7);
        if (dailyActivity >= 100) {
            activityBonus += 200;
        } else if (dailyActivity >= 50) {
            activityBonus += 100;
        } else if (dailyActivity >= 30) {
            activityBonus += 50;
        }

        // 4. Streak Bonusu
        int streakDays = getCurrentStreak(userId);
        int streakBonus = 0;
        if (streakDays >= 30) {
            streakBonus = 200;
        } else if (streakDays >= 7) {
            streakBonus = 50;
        } else if (streakDays >= 3) {
            streakBonus = 20;
        }

        // 5. Doğruluk Oranı Bonusu
        int accuracyBonus = 0;
        if (accuracy >= 0.95) {
            accuracyBonus = 100;
        } else if (accuracy >= 0.90) {
            accuracyBonus = 50;
        } else if (accuracy >= 0.80) {
            accuracyBonus = 30;
        }

        return new ScoreBreakdown(
            baseScore,
            netBonus,
            activityBonus,
            streakBonus,
            accuracyBonus
        );
    }

    /**
     * Kullanıcının mevcut streak gün sayısını hesaplar
     */
    private int getCurrentStreak(Long userId) {
        // Son aktivite tarihini al
        List<QuizOturumu> recentOturumlar = quizOturumuRepository.findByUserIdAndFinishedAtBetween(
            userId,
            Instant.now().minusSeconds(30L * 24 * 60 * 60), // Son 30 gün
            Instant.now()
        );

        if (recentOturumlar.isEmpty()) {
            return 0;
        }

        // En son aktivite tarihini bul
        Optional<Instant> lastActivity = recentOturumlar.stream()
            .filter(o -> o.getFinishedAt() != null)
            .map(QuizOturumu::getFinishedAt)
            .max(Instant::compareTo);

        if (lastActivity.isEmpty()) {
            return 0;
        }

        LocalDate lastDate = lastActivity.get()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

        LocalDate today = LocalDate.now();

        // Eğer bugün veya dün aktivite yoksa streak 0
        if (!lastDate.equals(today) && !lastDate.equals(today.minusDays(1))) {
            return 0;
        }

        // Ardışık günleri say
        int streak = 0;
        LocalDate checkDate = today;

        while (true) {
            LocalDate finalCheckDate = checkDate;
            boolean hasActivity = recentOturumlar.stream()
                .anyMatch(o -> {
                    if (o.getFinishedAt() == null) return false;
                    LocalDate oturumDate = o.getFinishedAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                    return oturumDate.equals(finalCheckDate);
                });

            if (hasActivity) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * Belirli gün sayısı içindeki aktivite sayısını döndürür
     */
    private long getDailyActivityCount(Long userId, int days) {
        Instant startDate = Instant.now().minusSeconds((long) days * 24 * 60 * 60);
        List<QuizOturumu> oturumlar = quizOturumuRepository.findByUserIdAndFinishedAtBetween(
            userId,
            startDate,
            Instant.now()
        );
        
        // Farklı günleri say
        return oturumlar.stream()
            .filter(o -> o.getFinishedAt() != null)
            .map(o -> o.getFinishedAt()
                .atZone(ZoneId.systemDefault())
                .toLocalDate())
            .distinct()
            .count();
    }
}

