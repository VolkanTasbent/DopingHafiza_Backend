package com.example.backend.controller;

import com.example.backend.model.CozumKaydi;
import com.example.backend.service.CozumKaydiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cozum")
public class CozumController {

    private final CozumKaydiService cozumService;

    public CozumController(CozumKaydiService cozumService) {
        this.cozumService = cozumService;
    }

    @PostMapping
    public ResponseEntity<CozumKaydi> kaydet(@RequestBody CozumKaydi kayit) {
        return ResponseEntity.ok(cozumService.kaydet(kayit));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CozumKaydi>> getKullaniciKayitlari(@PathVariable Long userId) {
        return ResponseEntity.ok(cozumService.kullaniciKayitlari(userId));
    }

    @GetMapping("/analiz/{userId}")
    public ResponseEntity<Map<String, Object>> konuAnalizi(@PathVariable Long userId) {
        return ResponseEntity.ok(cozumService.konuBazliAnaliz(userId));
    }

    @GetMapping("/gelisim/{userId}")
    public ResponseEntity<List<Object[]>> gelisimGrafik(@PathVariable Long userId) {
        return ResponseEntity.ok(cozumService.gelisimVerisi(userId));
    }
}
