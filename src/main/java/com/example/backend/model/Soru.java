package com.example.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "soru")
public class Soru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ders_id")
    private Ders ders;

    @ManyToMany
    @JoinTable(
            name = "soru_konu",
            joinColumns = @JoinColumn(name = "soru_id"),
            inverseJoinColumns = @JoinColumn(name = "konu_id")
    )
    private Set<Konu> konular = new LinkedHashSet<>();

    @Column(nullable = false, length = 1000)
    private String metin;

    @Column(length = 40)
    private String tip;

    private Integer zorluk;

    @Column(name = "soru_no")
    private Integer soruNo;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "aciklama")
    private String aciklama;

    @Column(name = "cozum_videosu_url", length = 500)
    private String cozumVideosuUrl;

    @Column(name = "olusturma_tarihi")
    private OffsetDateTime olusturmaTarihi;

    // 🔥 FLASHCARD İÇİN GEREKLİ: SEÇENEKLER
    @OneToMany(mappedBy = "soru", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Secenek> secenekler = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        if (olusturmaTarihi == null) {
            olusturmaTarihi = OffsetDateTime.now();
        }
    }

    // ---- GETTER - SETTER ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ders getDers() { return ders; }
    public void setDers(Ders ders) { this.ders = ders; }

    public Set<Konu> getKonular() { return konular; }
    public void setKonular(Set<Konu> konular) { this.konular = konular; }

    public String getMetin() { return metin; }
    public void setMetin(String metin) { this.metin = metin; }

    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }

    public Integer getZorluk() { return zorluk; }
    public void setZorluk(Integer zorluk) { this.zorluk = zorluk; }

    public Integer getSoruNo() { return soruNo; }
    public void setSoruNo(Integer soruNo) { this.soruNo = soruNo; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }

    public String getCozumVideosuUrl() { return cozumVideosuUrl; }
    public void setCozumVideosuUrl(String cozumVideosuUrl) { this.cozumVideosuUrl = cozumVideosuUrl; }

    public OffsetDateTime getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(OffsetDateTime olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }

    // 🔥 FLASHCARD İÇİN EKLENEN GETTER → BU OLMADIĞI İÇİN HATA ALIYORDUN!
    public Set<Secenek> getSecenekler() {
        return secenekler;
    }

    public void setSecenekler(Set<Secenek> secenekler) {
        this.secenekler = secenekler;
    }
}
