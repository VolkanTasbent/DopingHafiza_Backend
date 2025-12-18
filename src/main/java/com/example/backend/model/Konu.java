package com.example.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @Column(columnDefinition = "TEXT")
    private String aciklama;

    @OneToMany(mappedBy = "konu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("siralama ASC")
    private List<KonuVideo> videolar = new ArrayList<>();

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
    
    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }
    
    public List<KonuVideo> getVideolar() { return videolar; }
    public void setVideolar(List<KonuVideo> videolar) { this.videolar = videolar; }
}
