// src/main/java/com/example/backend/repository/QuizOturumuRepository.java
package com.example.backend.repository;

import com.example.backend.model.AppUser;
import com.example.backend.model.QuizOturumu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizOturumuRepository extends JpaRepository<QuizOturumu, Long> {
    Page<QuizOturumu> findByUser(AppUser user, Pageable pg);
    Optional<QuizOturumu> findByIdAndUser(Long id, AppUser user);
}
