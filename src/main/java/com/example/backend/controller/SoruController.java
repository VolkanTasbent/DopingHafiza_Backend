// src/main/java/com/example/backend/controller/SoruController.java
package com.example.backend.controller;

import com.example.backend.dto.SecenekDTO;
import com.example.backend.dto.SoruDTO;
import com.example.backend.service.SoruService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sorular")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class SoruController {

    private final SoruService service;
    public SoruController(SoruService service) { this.service = service; }

    @GetMapping
    public List<SoruDTO> liste(@RequestParam Long dersId,
                               @RequestParam(required = false) Long konuId,
                               @RequestParam(required = false) Integer limit,
                               @RequestParam(required = false) Integer page,
                               @RequestParam(required = false) Integer size) {
        if (konuId != null) {
            return service.getSorularByKonu(dersId, konuId, limit);
        }
        if (page != null || size != null) {
            int p = page != null ? Math.max(0, page) : 0;
            int s = size != null ? Math.max(1, size) : 10;
            return service.getSorularPaged(dersId, p, s);
        }
        return service.getSorular(dersId, limit);
    }

    /** Soru oluşturma: konuIds dizi + (opsiyonel) secenekler dizisi aynı istekte gelir */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public SoruDTO create(@RequestBody Map<String, Object> body) {
        Long dersId = requiredLong(body, "dersId");
        List<Long> konuIds = requiredLongList(body, "konuIds");
        String metin = requiredString(body, "metin");
        String tip = optionalString(body, "tip");
        Integer zorluk = optionalInteger(body, "zorluk");
        String imageUrl = optionalString(body, "imageUrl");
        String aciklama = optionalString(body, "aciklama");
        Integer soruNo = optionalInteger(body, "soruNo");

        SoruDTO created = service.addSoru(dersId, konuIds, metin, tip, zorluk, imageUrl, soruNo, aciklama);

        // varsa secenekler ekle (sende zaten vardı)
        Object seceneklerObj = body.get("secenekler");
        if (seceneklerObj instanceof List<?> raw && !raw.isEmpty()) {
            for (Object o : raw) {
                if (o instanceof Map<?,?> m) {
                    String smetin = Objects.toString(m.get("metin"), "").trim();
                    boolean dogru = m.get("dogru") != null && Boolean.parseBoolean(Objects.toString(m.get("dogru")));
                    Integer siralama = optionalInteger((Map<String,Object>) (Map<?,?>) m, "siralama");
                    if (!smetin.isEmpty()) service.addSecenek(created.id(), smetin, dogru, siralama);
                }
            }
        }
        return service.getById(created.id());
    }

    @PostMapping("/{soruId}/secenekler")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SecenekDTO addSecenek(@PathVariable Long soruId, @RequestBody Map<String, Object> body) {
        String metin = requiredString(body, "metin");
        boolean dogru = body.get("dogru") != null && Boolean.parseBoolean(Objects.toString(body.get("dogru")));
        Integer siralama = optionalInteger(body, "siralama");
        return service.addSecenek(soruId, metin, dogru, siralama);
    }

    @DeleteMapping("/secenekler/{secenekId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSecenek(@PathVariable Long secenekId) {
        service.deleteSecenek(secenekId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSoru(@PathVariable Long id) {
        service.deleteSoru(id);
    }

    // ---- helpers ----
    private static Long requiredLong(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) throw new IllegalArgumentException(key + " eksik");
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); }
        catch (Exception e) { throw new IllegalArgumentException(key + " sayısal olmalı"); }
    }

    @SuppressWarnings("unchecked")
    private static List<Long> requiredLongList(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) throw new IllegalArgumentException(key + " eksik");
        try {
            List<?> raw = (List<?>) v;
            if (raw.isEmpty()) throw new IllegalArgumentException(key + " boş olamaz");
            return raw.stream().map(o -> {
                if (o instanceof Number n) return n.longValue();
                return Long.parseLong(Objects.toString(o));
            }).toList();
        } catch (Exception e) {
            throw new IllegalArgumentException(key + " dizi (array) olmalı");
        }
    }

    private static String requiredString(Map<String, Object> body, String key) {
        String s = Objects.toString(body.get(key), "").trim();
        if (s.isEmpty()) throw new IllegalArgumentException(key + " boş olamaz");
        return s;
    }

    private static String optionalString(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : Objects.toString(v, null);
    }

    private static Integer optionalInteger(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); }
        catch (Exception e) { return null; }
    }
}
