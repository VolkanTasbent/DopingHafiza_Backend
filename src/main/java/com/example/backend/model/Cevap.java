package com.example.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cevap")
public class Cevap {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "oturum_id")
    private QuizOturumu oturum;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "soru_id")
    private Soru soru;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secenek_id")
    private Secenek secenek; // null olabilir

    @Column(nullable = false)
    private boolean dogru;

    public Long getId() { return id; }
    public QuizOturumu getOturum() { return oturum; }
    public Soru getSoru() { return soru; }
    public Secenek getSecenek() { return secenek; }
    public boolean isDogru() { return dogru; }

    public void setId(Long id) { this.id = id; }
    public void setOturum(QuizOturumu oturum) { this.oturum = oturum; }
    public void setSoru(Soru soru) { this.soru = soru; }
    public void setSecenek(Secenek secenek) { this.secenek = secenek; }
    public void setDogru(boolean dogru) { this.dogru = dogru; }
}
