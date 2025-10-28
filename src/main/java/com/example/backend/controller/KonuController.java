package com.example.backend.controller;

import com.example.backend.dto.KonuDTO;
import com.example.backend.model.Konu;
import com.example.backend.service.KonuService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                .map(k -> new KonuDTO(k.getId(), k.getAd(), k.getDokumanUrl(), k.getDokumanAdi()))
                .toList();
    }

    /** Yeni konu oluşturur (ADMIN) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public KonuDTO create(@RequestBody Map<String, Object> body) {
        Long dersId = toLongRequired(body.get("dersId"), "dersId");
        String ad = toStringRequired(body.get("ad"), "ad");
        Konu k = service.create(dersId, ad);
        return new KonuDTO(k.getId(), k.getAd(), k.getDokumanUrl(), k.getDokumanAdi());
    }

    // ---- yardımcılar ----
    private static Long toLongRequired(Object v, String key) {
        if (v == null) throw new IllegalArgumentException(key + " eksik");
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(Objects.toString(v)); }
        catch (Exception e) { throw new IllegalArgumentException(key + " sayısal olmalı"); }
    }

    private static String toStringRequired(Object v, String key) {
        String s = Objects.toString(v, "").trim();
        if (s.isEmpty()) throw new IllegalArgumentException(key + " boş olamaz");
        return s;
    }
}
