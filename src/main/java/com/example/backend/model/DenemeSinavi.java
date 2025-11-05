package com.example.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "deneme_sinavi")
public class DenemeSinavi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String ad;

    @Column(nullable = false, length = 10)
    private String tip; // "TYT" veya "AYT"

    @Column(name = "olusturma_tarihi")
    private OffsetDateTime olusturmaTarihi;

    @Column(columnDefinition = "TEXT")
    private String aciklama;

    @PrePersist
    protected void onCreate() {
        if (olusturmaTarihi == null) {
            olusturmaTarihi = OffsetDateTime.now();
        }
    }

    // ---- getters / setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }

    public OffsetDateTime getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(OffsetDateTime olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }

    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }
}


