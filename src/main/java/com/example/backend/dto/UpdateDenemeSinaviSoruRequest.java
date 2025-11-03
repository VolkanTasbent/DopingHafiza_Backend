package com.example.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateDenemeSinaviSoruRequest(
    @Size(max = 2000, message = "Soru metni maksimum 2000 karakter olabilir")
    String soruMetni,
    
    @Size(max = 1000, message = "Seçenek metni maksimum 1000 karakter olabilir")
    String sikA,
    
    @Size(max = 1000, message = "Seçenek metni maksimum 1000 karakter olabilir")
    String sikB,
    
    @Size(max = 1000, message = "Seçenek metni maksimum 1000 karakter olabilir")
    String sikC,
    
    @Size(max = 1000, message = "Seçenek metni maksimum 1000 karakter olabilir")
    String sikD,
    
    @Size(max = 1000, message = "Seçenek metni maksimum 1000 karakter olabilir")
    String sikE,
    
    @Pattern(regexp = "^[ABCDE]$", message = "Doğru cevap A, B, C, D veya E olmalıdır")
    String dogruCevap,
    
    Integer zorluk, // 1-5
    
    @Size(max = 500, message = "Konular maksimum 500 karakter olabilir")
    String konular,
    
    @Size(max = 2000, message = "Açıklama maksimum 2000 karakter olabilir")
    String aciklama,
    
    Integer soruNo,
    
    Long dersId
) {}

