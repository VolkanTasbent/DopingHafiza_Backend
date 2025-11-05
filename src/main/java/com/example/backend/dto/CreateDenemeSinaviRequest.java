package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDenemeSinaviRequest(
    @NotBlank(message = "Ad boş olamaz")
    @Size(max = 200, message = "Ad maksimum 200 karakter olabilir")
    String ad,
    
    @NotBlank(message = "Tip boş olamaz")
    @Pattern(regexp = "^(TYT|AYT)$", message = "Tip sadece TYT veya AYT olabilir")
    String tip,
    
    @Size(max = 1000, message = "Açıklama maksimum 1000 karakter olabilir")
    String aciklama
) {}


