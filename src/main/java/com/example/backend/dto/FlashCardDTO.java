package com.example.backend.dto;

import java.util.List;

public class FlashCardDTO {

    private Long id;
    private String soruMetni;
    private List<String> secenekler; // Tüm seçenek metinleri
    private String dogruSecenek;

    public FlashCardDTO(Long id, String soruMetni, List<String> secenekler, String dogruSecenek) {
        this.id = id;
        this.soruMetni = soruMetni;
        this.secenekler = secenekler;
        this.dogruSecenek = dogruSecenek;
    }

    public Long getId() { return id; }
    public String getSoruMetni() { return soruMetni; }
    public List<String> getSecenekler() { return secenekler; }
    public String getDogruSecenek() { return dogruSecenek; }
}
