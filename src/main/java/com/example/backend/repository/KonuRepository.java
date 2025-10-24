package com.example.backend.repository;

import com.example.backend.model.Ders;
import com.example.backend.model.Konu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KonuRepository extends JpaRepository<Konu, Long> {
    List<Konu> findByDersOrderByAdAsc(Ders ders);
}
