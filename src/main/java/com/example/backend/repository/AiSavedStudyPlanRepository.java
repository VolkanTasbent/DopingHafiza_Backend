package com.example.backend.repository;

import com.example.backend.model.AiSavedStudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiSavedStudyPlanRepository extends JpaRepository<AiSavedStudyPlan, Long> {

    List<AiSavedStudyPlan> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AiSavedStudyPlan> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<AiSavedStudyPlan> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
