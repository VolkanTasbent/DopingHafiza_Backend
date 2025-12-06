package com.example.backend.repository;

import com.example.backend.model.UserActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    
    List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT a FROM UserActivity a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    List<UserActivity> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
}

