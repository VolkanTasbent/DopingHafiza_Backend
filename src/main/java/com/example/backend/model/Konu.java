package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "konu", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ders_id", "ad"})
})
public class Konu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ders_id")
    private Ders ders;

    @Column(nullable = false, length = 150)
    private String ad;

    public Long getId() { return id; }
    public Ders getDers() { return ders; }
    public void setDers(Ders ders) { this.ders = ders; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }
}
