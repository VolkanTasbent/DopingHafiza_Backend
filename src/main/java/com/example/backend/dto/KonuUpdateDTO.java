package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KonuUpdateDTO {
    private String ad;
    private String aciklama;
    
    @JsonProperty("konuAnlatimVideosuUrl")
    private String konuAnlatimVideosuUrl;
    
    @JsonProperty("dokumanUrl")
    private String dokumanUrl;

    // Default constructor (Jackson için gerekli)
    public KonuUpdateDTO() {
    }

    // Getters and Setters
    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getKonuAnlatimVideosuUrl() {
        return konuAnlatimVideosuUrl;
    }

    public void setKonuAnlatimVideosuUrl(String konuAnlatimVideosuUrl) {
        this.konuAnlatimVideosuUrl = konuAnlatimVideosuUrl;
    }

    public String getDokumanUrl() {
        return dokumanUrl;
    }

    public void setDokumanUrl(String dokumanUrl) {
        this.dokumanUrl = dokumanUrl;
    }
}

