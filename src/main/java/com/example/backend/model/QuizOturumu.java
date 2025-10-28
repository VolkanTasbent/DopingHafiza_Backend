package com.example.backend.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "quiz_oturumu")
public class QuizOturumu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // YENİ: oturumu çözen kullanıcı
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;


    @Column(name="started_at")
    private Instant startedAt;

    @Column(name="finished_at")
    private Instant finishedAt;

    @Column(name="duration_ms")
    private Long durationMs;

    private Integer total;
    private Integer correct;
    private Integer wrong;
    private Integer empty;  // Boş bırakılan soru sayısı
    private Integer score;

    // Basitlik için kullanıcı alanını atlıyoruz (email vs.)
    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public Long getDurationMs() { return durationMs; }
    public Integer getTotal() { return total; }
    public Integer getCorrect() { return correct; }
    public Integer getWrong() { return wrong; }
    public Integer getEmpty() { return empty; }
    public Integer getScore() { return score; }

    public void setId(Long id) { this.id = id; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public void setTotal(Integer total) { this.total = total; }
    public void setCorrect(Integer correct) { this.correct = correct; }
    public void setWrong(Integer wrong) { this.wrong = wrong; }
    public void setEmpty(Integer empty) { this.empty = empty; }
    public void setScore(Integer score) { this.score = score; }
}
