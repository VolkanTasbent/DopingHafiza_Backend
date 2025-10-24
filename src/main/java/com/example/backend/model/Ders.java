package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ders")
public class Ders {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String ad;

    public Ders() {}
    public Ders(String ad) { this.ad = ad; }

    public Long getId() { return id; }
    public String getAd() { return ad; }
    public void setId(Long id) { this.id = id; }
    public void setAd(String ad) { this.ad = ad; }
}
