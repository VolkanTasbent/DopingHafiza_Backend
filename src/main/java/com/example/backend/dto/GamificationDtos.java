package com.example.backend.dto;

import java.util.List;

public class GamificationDtos {

    public record ActiveEffectDTO(
            String id,
            String expiresAt
    ) {}

    public record DailyTaskDTO(
            String id,
            String title,
            String metric,
            Integer target,
            Integer rewardPoints,
            Integer rewardGold,
            Boolean completed,
            String assignedBy
    ) {}

    public record GamificationStateDTO(
            Integer points,
            Integer gold,
            String dailyTaskDate,
            List<DailyTaskDTO> dailyTasks,
            List<String> ownedItems,
            List<ActiveEffectDTO> activeEffects,
            Integer solved,
            Integer correct,
            Integer sessions
    ) {}

    public record GamificationSyncResponseDTO(
            GamificationStateDTO state,
            Integer earnedPoints,
            Integer earnedGold
    ) {}

    public record GamificationPurchaseRequestDTO(String itemId) {}

    public record GamificationTaskCompleteRequestDTO(String taskId) {}

    public record MarketItemDTO(
            String id,
            String label,
            String desc,
            Integer price
    ) {}
}
