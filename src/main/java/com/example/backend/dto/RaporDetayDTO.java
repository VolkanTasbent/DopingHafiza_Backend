package com.example.backend.dto;

import java.util.List;

public record RaporDetayDTO(Long oturumId, List<RaporDetayItemDTO> items) {}
