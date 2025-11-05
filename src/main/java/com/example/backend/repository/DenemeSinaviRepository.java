package com.example.backend.repository;

import com.example.backend.model.DenemeSinavi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DenemeSinaviRepository extends JpaRepository<DenemeSinavi, Long> {
    List<DenemeSinavi> findByTipOrderByOlusturmaTarihiDesc(String tip);
    List<DenemeSinavi> findAllByOrderByOlusturmaTarihiDesc();
}


