package com.example.backend.repository;
import com.example.backend.model.Cevap;
import com.example.backend.model.QuizOturumu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CevapRepository extends JpaRepository<Cevap, Long> {
    List<Cevap> findByOturum(QuizOturumu o);
}
