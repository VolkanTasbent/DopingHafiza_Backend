package com.example.backend.repository;

import com.example.backend.model.Konu;
import com.example.backend.model.KonuVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KonuVideoRepository extends JpaRepository<KonuVideo, Long> {
    List<KonuVideo> findByKonuOrderBySiralamaAsc(Konu konu);
    
    @Query("SELECT kv FROM KonuVideo kv WHERE kv.konu.id = :konuId ORDER BY kv.siralama ASC")
    List<KonuVideo> findByKonuIdOrderBySiralamaAsc(@Param("konuId") Long konuId);
    
    void deleteByKonu(Konu konu);
}



