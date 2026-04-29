package com.example.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> ok() {
        return Map.of("status", "OK");
    }

    @GetMapping("/uptime")
    public Map<String, String> uptime() {
        return Map.of("status", "UP");
    }
}
