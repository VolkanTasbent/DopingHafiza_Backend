package com.example.backend.repository;
import com.example.backend.model.Secenek;
import com.example.backend.model.Soru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SecenekRepository extends JpaRepository<Secenek, Long> {
    List<Secenek> findBySoruOrderBySiralamaAscIdAsc(Soru soru);

    @Query("""
        SELECT o FROM Secenek o
        JOIN FETCH o.soru s
        WHERE s.id IN :soruIds
        ORDER BY s.id ASC, o.siralama ASC, o.id ASC
        """)
    List<Secenek> findBySoruIdIn(@Param("soruIds") Collection<Long> soruIds);
}
