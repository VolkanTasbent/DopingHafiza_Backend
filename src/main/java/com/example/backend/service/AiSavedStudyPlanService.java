package com.example.backend.service;

import com.example.backend.dto.AiSaveStudyPlanRequestDTO;
import com.example.backend.dto.AiSavedStudyPlanResponseDTO;
import com.example.backend.dto.AiStudyTaskDTO;
import com.example.backend.model.AiSavedStudyPlan;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AiSavedStudyPlanRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiSavedStudyPlanService {

    private static final int MAX_PLANS_PER_USER = 20;

    private final AiSavedStudyPlanRepository repository;
    private final ObjectMapper objectMapper;

    public AiSavedStudyPlanService(AiSavedStudyPlanRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public List<AiSavedStudyPlanResponseDTO> listForUser(AppUser user) {
        return repository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AiSavedStudyPlanResponseDTO save(AppUser user, AiSaveStudyPlanRequestDTO req) {
        PlanPayload payload = new PlanPayload(
                req.summary(),
                req.analyzedDays(),
                req.dailyMinutes(),
                req.mode() != null ? req.mode() : "mixed",
                req.tasks() != null ? req.tasks() : List.of(),
                req.focusTips() != null ? req.focusTips() : List.of(),
                req.weakTopicsPreview() != null ? req.weakTopicsPreview() : List.of()
        );

        AiSavedStudyPlan entity = new AiSavedStudyPlan();
        entity.setUserId(user.getId());
        entity.setTitle(req.title().trim());
        try {
            entity.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Plan serilestirilemedi.", e);
        }
        entity.setCreatedAt(Instant.now());
        repository.save(entity);

        trimExcessOldest(user.getId());
        return toResponse(entity);
    }

    private void trimExcessOldest(Long userId) {
        while (repository.countByUserId(userId) > MAX_PLANS_PER_USER) {
            List<AiSavedStudyPlan> asc = repository.findByUserIdOrderByCreatedAtAsc(userId);
            if (asc.isEmpty()) break;
            repository.delete(asc.get(0));
        }
    }

    @Transactional
    public boolean delete(AppUser user, Long planId) {
        return repository.findByIdAndUserId(planId, user.getId())
                .map(entity -> {
                    repository.delete(entity);
                    return true;
                })
                .orElse(false);
    }

    private AiSavedStudyPlanResponseDTO toResponse(AiSavedStudyPlan entity) {
        PlanPayload p = readPayload(entity.getPayloadJson());
        return new AiSavedStudyPlanResponseDTO(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getTitle(),
                p.summary,
                p.analyzedDays,
                p.dailyMinutes,
                p.mode,
                p.tasks != null ? p.tasks : List.of(),
                p.focusTips != null ? p.focusTips : List.of(),
                p.weakTopicsPreview != null ? p.weakTopicsPreview : List.of()
        );
    }

    private PlanPayload readPayload(String json) {
        if (json == null || json.isBlank()) {
            return new PlanPayload(null, null, null, "mixed", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        try {
            return objectMapper.readValue(json, PlanPayload.class);
        } catch (Exception e) {
            return new PlanPayload(null, null, null, "mixed", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PlanPayload {
        public String summary;
        public Integer analyzedDays;
        public Integer dailyMinutes;
        public String mode;
        public List<AiStudyTaskDTO> tasks;
        public List<String> focusTips;
        public List<String> weakTopicsPreview;

        public PlanPayload() {}

        public PlanPayload(
                String summary,
                Integer analyzedDays,
                Integer dailyMinutes,
                String mode,
                List<AiStudyTaskDTO> tasks,
                List<String> focusTips,
                List<String> weakTopicsPreview
        ) {
            this.summary = summary;
            this.analyzedDays = analyzedDays;
            this.dailyMinutes = dailyMinutes;
            this.mode = mode;
            this.tasks = tasks;
            this.focusTips = focusTips;
            this.weakTopicsPreview = weakTopicsPreview;
        }
    }
}
