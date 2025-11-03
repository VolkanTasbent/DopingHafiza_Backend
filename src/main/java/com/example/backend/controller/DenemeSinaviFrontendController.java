package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.DenemeSinaviService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Frontend için deneme sınavı endpoint'leri
 * Frontend'in beklediği format: { adi, kategori } yerine { ad, tip }
 */
@RestController
@RequestMapping("/api/deneme-sinavi")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class DenemeSinaviFrontendController {
    private final DenemeSinaviService service;

    public DenemeSinaviFrontendController(DenemeSinaviService service) {
        this.service = service;
    }

    /** Deneme sınavı oluştur (ADMIN) - Frontend format (adi, kategori) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DenemeSinaviResponseDTO createDenemeSinavi(@Valid @RequestBody CreateDenemeSinaviRequestFrontend request) {
        CreateDenemeSinaviRequest backendReq = request.toBackendRequest();
        DenemeSinaviDTO dto = service.create(backendReq);
        return DenemeSinaviResponseDTO.from(dto);
    }

    /** Tüm deneme sınavlarını listele - Frontend format */
    @GetMapping
    public List<DenemeSinaviResponseDTO> getAllDenemeSinavlari() {
        return service.listAll().stream()
                .map(DenemeSinaviResponseDTO::from)
                .toList();
    }

    /** Deneme sınavı detayı - Frontend format */
    @GetMapping("/{id}")
    public DenemeSinaviResponseDTO getDenemeSinaviById(@PathVariable Long id) {
        DenemeSinaviDTO dto = service.getById(id);
        return DenemeSinaviResponseDTO.from(dto);
    }
}

