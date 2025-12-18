package com.example.backend.repository;

import com.example.backend.model.Ders;
import com.example.backend.model.Konu;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KonuRepository extends JpaRepository<Konu, Long> {
    List<Konu> findByDersOrderByAdAsc(Ders ders);
    
    // Ders ile birlikte fetch et (lazy loading sorununu çözmek için)
    @EntityGraph(attributePaths = {"ders"})
    Optional<Konu> findWithDersById(Long id);
}
