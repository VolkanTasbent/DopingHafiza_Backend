package com.example.backend.dto;

import java.util.List;

public record KonuDTO(
    Long id, 
    String ad, 
    String dokumanUrl, 
    String dokumanAdi, 
    String konuAnlatimVideosuUrl,
    String aciklama,
    Long dersId,
    List<KonuVideoDTO> videolar
) {}
