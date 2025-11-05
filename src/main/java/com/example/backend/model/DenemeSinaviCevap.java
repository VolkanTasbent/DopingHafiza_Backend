package com.example.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "deneme_sinavi_cevap",
       uniqueConstraints = @UniqueConstraint(columnNames = {"oturum_id", "soru_no"}))
public class DenemeSinaviCevap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "oturum_id")
    private QuizOturumu oturum;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deneme_sinavi_soru_id")
    private DenemeSinaviSoru denemeSinaviSoru;

    @Column(name = "soru_no", nullable = false)
    private Integer soruNo;

    @Column(name = "secilen_cevap", length = 1)
    private String secilenCevap; // "A", "B", "C", "D", "E" veya null (boş)

    @Column(nullable = false)
    private boolean dogru;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuizOturumu getOturum() { return oturum; }
    public void setOturum(QuizOturumu oturum) { this.oturum = oturum; }

    public DenemeSinaviSoru getDenemeSinaviSoru() { return denemeSinaviSoru; }
    public void setDenemeSinaviSoru(DenemeSinaviSoru denemeSinaviSoru) { this.denemeSinaviSoru = denemeSinaviSoru; }

    public Integer getSoruNo() { return soruNo; }
    public void setSoruNo(Integer soruNo) { this.soruNo = soruNo; }

    public String getSecilenCevap() { return secilenCevap; }
    public void setSecilenCevap(String secilenCevap) { this.secilenCevap = secilenCevap; }

    public boolean isDogru() { return dogru; }
    public void setDogru(boolean dogru) { this.dogru = dogru; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

