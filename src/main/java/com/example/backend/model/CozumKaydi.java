package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cozum_kaydi")
public class CozumKaydi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soru_id")
    private Soru soru;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "konu_id")
    private Konu konu;

    @Column(name = "dogru_mu")
    private boolean dogruMu;

    @Column(name = "sure")
    private Integer sure; // saniye cinsinden süre

    @Column(name = "tarih")
    private LocalDateTime tarih = LocalDateTime.now();

    // --- GETTER / SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public Soru getSoru() { return soru; }
    public void setSoru(Soru soru) { this.soru = soru; }

    public Konu getKonu() { return konu; }
    public void setKonu(Konu konu) { this.konu = konu; }

    public boolean isDogruMu() { return dogruMu; }
    public void setDogruMu(boolean dogruMu) { this.dogruMu = dogruMu; }

    public Integer getSure() { return sure; }
    public void setSure(Integer sure) { this.sure = sure; }

    public LocalDateTime getTarih() { return tarih; }
    public void setTarih(LocalDateTime tarih) { this.tarih = tarih; }
}
