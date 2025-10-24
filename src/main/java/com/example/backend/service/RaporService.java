// src/main/java/com/example/backend/service/RaporService.java
package com.example.backend.service;

import com.example.backend.dto.OturumDetayDTO;
import com.example.backend.repository.RaporRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RaporService {

    private final RaporRepository raporRepository;

    public RaporService(RaporRepository raporRepository) {
        this.raporRepository = raporRepository;
    }

    public List<OturumDetayDTO> getOturumDetay(Long oturumId, boolean sadeceYanlis) {
        List<Object[]> rows = raporRepository.findOturumDetay(oturumId, sadeceYanlis);
        List<OturumDetayDTO> list = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            // Sıra: soru_id, soru_metin, konu_ad, ders_ad, secilen_secenek, dogru_secenek, dogru_mu
            list.add(new OturumDetayDTO(
                    r[0] != null ? ((Number) r[0]).longValue() : null,
                    (String) r[1],
                    (String) r[2],
                    (String) r[3],
                    (String) r[4],
                    (String) r[5],
                    r[6] != null ? ((Boolean) r[6]) : null
            ));
        }
        return list;
    }
}
