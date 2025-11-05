package com.example.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

// Ders için import - circular dependency olmaması için full qualified name kullanıyoruz

@Entity
@Table(name = "deneme_sinavi_soru",
       uniqueConstraints = @UniqueConstraint(columnNames = {"deneme_sinavi_id", "soru_no"}))
public class DenemeSinaviSoru {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "deneme_sinavi_id")
    private DenemeSinavi denemeSinavi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ders_id")
    private Ders ders;

    @Column(name = "soru_no", nullable = false)
    private Integer soruNo;

    @Column(name = "soru_metni", nullable = false, columnDefinition = "TEXT")
    private String soruMetni;

    @Column(name = "sik_a", columnDefinition = "TEXT")
    private String sikA;

    @Column(name = "sik_b", columnDefinition = "TEXT")
    private String sikB;

    @Column(name = "sik_c", columnDefinition = "TEXT")
    private String sikC;

    @Column(name = "sik_d", columnDefinition = "TEXT")
    private String sikD;

    @Column(name = "sik_e", columnDefinition = "TEXT")
    private String sikE;

    @Column(name = "dogru_cevap", nullable = false, length = 1)
    private String dogruCevap; // "A", "B", "C", "D", "E"

    @Column(name = "zorluk")
    private Integer zorluk; // 1-5

    @Column(name = "konular", columnDefinition = "TEXT")
    private String konular; // Virgülle ayrılmış konu adları

    @Column(name = "aciklama", columnDefinition = "TEXT")
    private String aciklama;

    @Column(name = "cozum_videosu_url", length = 500)
    private String cozumVideosuUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

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

    public DenemeSinavi getDenemeSinavi() { return denemeSinavi; }
    public void setDenemeSinavi(DenemeSinavi denemeSinavi) { this.denemeSinavi = denemeSinavi; }

    public Ders getDers() { return ders; }
    public void setDers(Ders ders) { this.ders = ders; }

    public Integer getSoruNo() { return soruNo; }
    public void setSoruNo(Integer soruNo) { this.soruNo = soruNo; }

    public String getSoruMetni() { return soruMetni; }
    public void setSoruMetni(String soruMetni) { this.soruMetni = soruMetni; }

    public String getSikA() { return sikA; }
    public void setSikA(String sikA) { this.sikA = sikA; }

    public String getSikB() { return sikB; }
    public void setSikB(String sikB) { this.sikB = sikB; }

    public String getSikC() { return sikC; }
    public void setSikC(String sikC) { this.sikC = sikC; }

    public String getSikD() { return sikD; }
    public void setSikD(String sikD) { this.sikD = sikD; }

    public String getSikE() { return sikE; }
    public void setSikE(String sikE) { this.sikE = sikE; }

    public String getDogruCevap() { return dogruCevap; }
    public void setDogruCevap(String dogruCevap) { this.dogruCevap = dogruCevap; }

    public Integer getZorluk() { return zorluk; }
    public void setZorluk(Integer zorluk) { this.zorluk = zorluk; }

    public String getKonular() { return konular; }
    public void setKonular(String konular) { this.konular = konular; }

    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }

    public String getCozumVideosuUrl() { return cozumVideosuUrl; }
    public void setCozumVideosuUrl(String cozumVideosuUrl) { this.cozumVideosuUrl = cozumVideosuUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public OffsetDateTime getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(OffsetDateTime olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }
}

