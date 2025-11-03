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

    @Column(name = "dokuman_url", length = 500)
    private String dokumanUrl;

    @Column(name = "dokuman_adi", length = 255)
    private String dokumanAdi;

    @Column(name = "konu_anlatim_videosu_url", length = 500)
    private String konuAnlatimVideosuUrl;

    public Long getId() { return id; }
    public Ders getDers() { return ders; }
    public void setDers(Ders ders) { this.ders = ders; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }
    
    public String getDokumanUrl() { return dokumanUrl; }
    public void setDokumanUrl(String dokumanUrl) { this.dokumanUrl = dokumanUrl; }
    
    public String getDokumanAdi() { return dokumanAdi; }
    public void setDokumanAdi(String dokumanAdi) { this.dokumanAdi = dokumanAdi; }
    
    public String getKonuAnlatimVideosuUrl() { return konuAnlatimVideosuUrl; }
    public void setKonuAnlatimVideosuUrl(String konuAnlatimVideosuUrl) { this.konuAnlatimVideosuUrl = konuAnlatimVideosuUrl; }
}
