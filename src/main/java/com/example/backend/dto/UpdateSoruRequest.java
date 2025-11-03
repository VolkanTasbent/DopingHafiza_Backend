package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateSoruRequest(
    // Opsiyonel alanlar (null veya boş ise güncellenmez)
    @Size(max = 1000, message = "Soru metni maksimum 1000 karakter olabilir")
    String metin,  // null veya boş ise güncellenmez
    
    @Size(max = 40, message = "Tip maksimum 40 karakter olabilir")
    String tip,  // null ise güncellenmez
    
    Integer zorluk,  // 1-5 arası olmalı, null ise güncellenmez
    
    @Size(max = 500, message = "Image URL maksimum 500 karakter olabilir")
    String imageUrl,  // null ise güncellenmez
    
    String aciklama,  // null ise güncellenmez
    
    @Size(max = 500, message = "Çözüm videosu URL maksimum 500 karakter olabilir")
    String cozumVideosuUrl,  // null ise güncellenmez
    
    Integer soruNo,  // null ise güncellenmez
    
    List<Long> konuIds,  // null veya boş ise güncellenmez
    
    List<UpdateSecenekRequest> secenekler  // null veya boş ise güncellenmez
) {
    // Compact constructor - metin ve cozumVideosuUrl'i trim et
    public UpdateSoruRequest {
        if (metin != null) {
            metin = metin.trim();
        }
        if (cozumVideosuUrl != null) {
            cozumVideosuUrl = cozumVideosuUrl.trim();
            // Boş string ise null yap (opsiyonel field için)
            if (cozumVideosuUrl.isEmpty()) {
                cozumVideosuUrl = null;
            }
        }
        if (zorluk != null && (zorluk < 1 || zorluk > 5)) {
            throw new IllegalArgumentException("Zorluk 1-5 arası olmalı");
        }
    }
}

