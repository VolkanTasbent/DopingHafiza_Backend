// src/main/java/com/example/backend/dto/OturumDetayDTO.java
package com.example.backend.dto;

public class OturumDetayDTO {
    private Long soruId;
    private String soruMetin;
    private String konuAd;
    private String dersAd;
    private String secilenSecenek; // kullanıcının seçtiği şık (metin)
    private String dogruSecenek;   // sorunun doğru şıkkı (metin)
    private Boolean dogruMu;

    public OturumDetayDTO(Long soruId, String soruMetin, String konuAd, String dersAd,
                          String secilenSecenek, String dogruSecenek, Boolean dogruMu) {
        this.soruId = soruId;
        this.soruMetin = soruMetin;
        this.konuAd = konuAd;
        this.dersAd = dersAd;
        this.secilenSecenek = secilenSecenek;
        this.dogruSecenek = dogruSecenek;
        this.dogruMu = dogruMu;
    }

    public Long getSoruId() { return soruId; }
    public String getSoruMetin() { return soruMetin; }
    public String getKonuAd() { return konuAd; }
    public String getDersAd() { return dersAd; }
    public String getSecilenSecenek() { return secilenSecenek; }
    public String getDogruSecenek() { return dogruSecenek; }
    public Boolean getDogruMu() { return dogruMu; }
}
