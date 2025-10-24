package com.example.backend.dto;

import lombok.Data;

@Data
public class CozumKaydiDto {
    private Long userId;
    private Long soruId;
    private Long konuId;
    private boolean dogruMu;
    private Integer sure; // saniye
}
