package com.example.backend.repository;
import com.example.backend.model.Secenek;
import com.example.backend.model.Soru;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SecenekRepository extends JpaRepository<Secenek, Long> {
    List<Secenek> findBySoruOrderBySiralamaAscIdAsc(Soru soru);
}
