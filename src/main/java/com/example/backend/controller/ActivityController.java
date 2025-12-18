package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.model.AppUser;
import com.example.backend.model.UserActivity;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.repository.UserActivityRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class ActivityController {

    private final UserActivityRepository activityRepository;
    private final AppUserRepository userRepository;

    public ActivityController(UserActivityRepository activityRepository, AppUserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecentActivitiesResponse> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Limit kontrolü
        int actualLimit = Math.min(Math.max(limit, 1), 50);

        List<UserActivity> activities = activityRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, actualLimit));

        List<ActivityResponse> activityResponses = activities.stream()
                .map(ActivityResponse::from)
                .collect(Collectors.toList());

        RecentActivitiesResponse response = new RecentActivitiesResponse(activityResponses);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityResponse> createActivity(
            @Valid @RequestBody CreateActivityRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserActivity activity = new UserActivity();
        activity.setUserId(user.getId());
        activity.setActivityType(request.getActivityType());
        activity.setActivityTitle(request.getActivityTitle());
        activity.setActivitySubtitle(request.getActivitySubtitle());
        activity.setActivityIcon(request.getActivityIcon() != null 
                ? request.getActivityIcon() 
                : "document");
        activity.setDersId(request.getDersId());
        activity.setKonuId(request.getKonuId());
        activity.setRaporId(request.getRaporId());
        activity.setMetadata(request.getMetadata());

        UserActivity saved = activityRepository.save(activity);

        return ResponseEntity.ok(ActivityResponse.from(saved));
    }
}







