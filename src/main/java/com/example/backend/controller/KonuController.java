package com.example.backend.controller;

import com.example.backend.dto.CreateKonuRequest;
import com.example.backend.dto.KonuDTO;
import com.example.backend.model.Konu;
import com.example.backend.service.KonuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/konu")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class KonuController {

    private final KonuService service;

    public KonuController(KonuService service) {
        this.service = service;
    }

    /** Belirli derse ait konuları listeler */
    @GetMapping
    public List<KonuDTO> list(@RequestParam Long dersId) {
        return service.listByDers(dersId).stream()
                .map(k -> new KonuDTO(k.getId(), k.getAd(), k.getDokumanUrl(), k.getDokumanAdi(), k.getKonuAnlatimVideosuUrl()))
                .toList();
    }

    /** Yeni konu oluşturur (ADMIN) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public KonuDTO create(@Valid @RequestBody CreateKonuRequest req) {
        // ✅ Tip güvenli! Spring otomatik validation yapar
        // ✅ dersId null veya negatif ise → 400 Bad Request
        // ✅ ad boş ise → 400 Bad Request
        Konu k = service.create(req.dersId(), req.ad());
        return new KonuDTO(k.getId(), k.getAd(), k.getDokumanUrl(), k.getDokumanAdi(), k.getKonuAnlatimVideosuUrl());
    }
}
