package com.example.backend.repository;

import com.example.backend.model.DenemeSinaviCevap;
import com.example.backend.model.QuizOturumu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DenemeSinaviCevapRepository extends JpaRepository<DenemeSinaviCevap, Long> {
    List<DenemeSinaviCevap> findByOturumOrderBySoruNoAsc(QuizOturumu oturum);
}

