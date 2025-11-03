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

    // soru.ders_id -> ders(id)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ders_id")
    private Ders ders;

    // N:N -> soru_konu(soru_id, konu_id)
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
    private String tip;              // "coktan_secmeli" vb.

    private Integer zorluk;          // 1-5

    @Column(name = "soru_no")
    private Integer soruNo;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "aciklama")
    private String aciklama;

    @Column(name = "cozum_videosu_url", length = 500)
    private String cozumVideosuUrl;

    // DB'de timestamptz ile uyumlu
    @Column(name = "olusturma_tarihi")
    private OffsetDateTime olusturmaTarihi;

    @PrePersist
    protected void onCreate() {
        if (olusturmaTarihi == null) {
            olusturmaTarihi = OffsetDateTime.now();
        }
    }

    // ---- getters / setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ders getDers() { return ders; }
    public void setDers(Ders ders) { this.ders = ders; }

    public Set<Konu> getKonular() { return konular; }
    public void setKonular(Set<Konu> konular) { this.konular = konular; }
    public void addKonu(Konu k) { this.konular.add(k); }
    public void removeKonu(Konu k) { this.konular.remove(k); }

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
}
