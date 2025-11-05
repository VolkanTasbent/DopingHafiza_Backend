package com.example.backend.repository;

import com.example.backend.model.DenemeSinavi;
import com.example.backend.model.DenemeSinaviSoru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DenemeSinaviSoruRepository extends JpaRepository<DenemeSinaviSoru, Long> {
    List<DenemeSinaviSoru> findByDenemeSinaviOrderBySoruNoAsc(DenemeSinavi denemeSinavi);
    
    @Query("SELECT MAX(ds.soruNo) FROM DenemeSinaviSoru ds WHERE ds.denemeSinavi = :deneme")
    Integer findMaxSoruNoByDenemeSinavi(@Param("deneme") DenemeSinavi deneme);
    
    Optional<DenemeSinaviSoru> findByDenemeSinaviAndSoruNo(DenemeSinavi denemeSinavi, Integer soruNo);
    
    void deleteByDenemeSinavi(DenemeSinavi denemeSinavi);
}


