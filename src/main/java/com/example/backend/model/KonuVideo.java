package com.example.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "konu_video")
public class KonuVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "konu_id", nullable = false)
    private Konu konu;

    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    @Column(name = "video_adi", length = 255)
    private String videoAdi;

    @Column(name = "siralama", nullable = false)
    private Integer siralama = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Konu getKonu() {
        return konu;
    }

    public void setKonu(Konu konu) {
        this.konu = konu;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoAdi() {
        return videoAdi;
    }

    public void setVideoAdi(String videoAdi) {
        this.videoAdi = videoAdi;
    }

    public Integer getSiralama() {
        return siralama;
    }

    public void setSiralama(Integer siralama) {
        this.siralama = siralama;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}


