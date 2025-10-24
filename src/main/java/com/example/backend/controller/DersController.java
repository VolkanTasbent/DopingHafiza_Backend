package com.example.backend.controller;

import com.example.backend.model.Ders;
import com.example.backend.service.DersService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ders")
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000"}, allowCredentials = "true")
public class DersController {
    private final DersService service;
    public DersController(DersService service) { this.service = service; }

    @GetMapping
    public List<Ders> all() {
        return service.getAll();
    }

    @PostMapping
    public Ders add(@RequestBody Ders ders) {
        return service.add(ders.getAd());
    }
}
