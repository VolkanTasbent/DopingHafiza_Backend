package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "secenek")
public class Secenek {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "soru_id")
    private Soru soru;

    @Column(nullable = false, length = 500)
    private String metin;

    @Column(nullable = false)
    private boolean dogru;

    private Integer siralama;

    public Long getId() { return id; }
    public Soru getSoru() { return soru; }
    public String getMetin() { return metin; }
    public boolean isDogru() { return dogru; }
    public Integer getSiralama() { return siralama; }

    public void setId(Long id) { this.id = id; }
    public void setSoru(Soru soru) { this.soru = soru; }
    public void setMetin(String metin) { this.metin = metin; }
    public void setDogru(boolean dogru) { this.dogru = dogru; }
    public void setSiralama(Integer siralama) { this.siralama = siralama; }
}
