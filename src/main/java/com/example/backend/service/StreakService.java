package com.example.backend.service;

import com.example.backend.model.AppUser;
import com.example.backend.model.UserStreak;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.UserStreakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class StreakService {
    private final UserStreakRepository streakRepository;
    private final AppUserRepository userRepository;

    public StreakService(UserStreakRepository streakRepository, AppUserRepository userRepository) {
        this.streakRepository = streakRepository;
        this.userRepository = userRepository;
    }

    /**
     * Kullanıcının streak'ini günceller
     * Giriş yapıldığında veya aktivite yapıldığında çağrılır
     */
    @Transactional
    public void updateStreak(Long userId, LocalDate activityDate) {
        // Kullanıcıyı kontrol et
        AppUser user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı: " + userId));

        // Streak kaydını bul veya oluştur
        Optional<UserStreak> streakOpt = streakRepository.findByUserId(userId);
        UserStreak streak = streakOpt.orElseGet(() -> {
            UserStreak newStreak = new UserStreak();
            newStreak.setUser(user);
            newStreak.setCurrentStreak(0);
            newStreak.setLongestStreak(0);
            return newStreak;
        });

        LocalDate today = LocalDate.now();
        LocalDate lastActivity = streak.getLastActivityDate();

        // Eğer bugün zaten aktivite kaydedilmişse, tekrar güncelleme
        if (lastActivity != null && lastActivity.equals(today)) {
            return; // Bugün zaten güncellenmiş
        }

        // Streak hesaplama mantığı
        if (lastActivity == null) {
            // İlk aktivite
            streak.setCurrentStreak(1);
            streak.setLastActivityDate(today);
        } else if (lastActivity.equals(today.minusDays(1))) {
            // Dün aktivite vardı, streak devam ediyor
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            streak.setLastActivityDate(today);
        } else if (lastActivity.equals(today)) {
            // Bugün zaten aktivite var (yukarıda kontrol edildi ama yine de)
            return;
        } else {
            // Streak kırıldı, sıfırla
            streak.setCurrentStreak(1);
            streak.setLastActivityDate(today);
        }

        // En uzun streak'i güncelle
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        // Kaydet
        streakRepository.save(streak);
    }

    /**
     * Kullanıcının streak bilgilerini döndürür
     */
    public StreakInfo getStreakInfo(Long userId) {
        Optional<UserStreak> streakOpt = streakRepository.findByUserId(userId);
        
        if (streakOpt.isEmpty()) {
            return new StreakInfo(0, 0, null);
        }

        UserStreak streak = streakOpt.get();
        return new StreakInfo(
            streak.getCurrentStreak(),
            streak.getLongestStreak(),
            streak.getLastActivityDate()
        );
    }

    /**
     * Streak bilgilerini taşıyan DTO
     */
    public record StreakInfo(
        int currentStreak,
        int longestStreak,
        LocalDate lastActivityDate
    ) {}
}




