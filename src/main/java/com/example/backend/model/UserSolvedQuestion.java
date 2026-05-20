package com.example.backend.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "user_solved_question",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_solved_question", columnNames = {"user_id", "soru_id"})
)
public class UserSolvedQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soru_id", nullable = false)
    private Soru soru;

    @Column(nullable = false)
    private boolean cozuldu = true;

    @Column(name = "cozuldu_at", nullable = false)
    private Instant cozulduAt = Instant.now();

    @Column(name = "dogru")
    private Boolean dogru;

    /** BOS = tekrar gelir; DOGRU / YANLIS = listeden atlanir */
    @Column(name = "son_durum", nullable = false, length = 10)
    private String sonDurum = "BOS";

    @Column(name = "oturum_id")
    private Long oturumId;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Soru getSoru() { return soru; }
    public boolean isCozuldu() { return cozuldu; }
    public Instant getCozulduAt() { return cozulduAt; }
    public Boolean getDogru() { return dogru; }
    public String getSonDurum() { return sonDurum; }
    public Long getOturumId() { return oturumId; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setSoru(Soru soru) { this.soru = soru; }
    public void setCozuldu(boolean cozuldu) { this.cozuldu = cozuldu; }
    public void setCozulduAt(Instant cozulduAt) { this.cozulduAt = cozulduAt; }
    public void setDogru(Boolean dogru) { this.dogru = dogru; }
    public void setSonDurum(String sonDurum) { this.sonDurum = sonDurum; }
    public void setOturumId(Long oturumId) { this.oturumId = oturumId; }
}
