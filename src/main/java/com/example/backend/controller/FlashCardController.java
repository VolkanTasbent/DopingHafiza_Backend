package com.example.backend.controller;

import com.example.backend.dto.FlashCardDTO;
import com.example.backend.model.Secenek;
import com.example.backend.model.Soru;
import com.example.backend.repository.SoruRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@CrossOrigin("*")
public class FlashCardController {

    private final SoruRepository soruRepository;

    public FlashCardController(SoruRepository soruRepository) {
        this.soruRepository = soruRepository;
    }

    @GetMapping
    public List<FlashCardDTO> getFlashCards(@RequestParam Long dersId) {

        List<Soru> sorular = soruRepository
                .findRandomQuestionsWithSecenek(dersId)
                .stream()
                .limit(20)
                .toList();

        return sorular.stream()
                .map(soru -> {

                    List<String> secenekler = soru.getSecenekler()
                            .stream()
                            .map(Secenek::getMetin)
                            .toList();

                    String dogru = soru.getSecenekler()
                            .stream()
                            .filter(Secenek::isDogru)
                            .findFirst()
                            .map(Secenek::getMetin)
                            .orElse(null);

                    return new FlashCardDTO(
                            soru.getId(),
                            soru.getMetin(),
                            secenekler,
                            dogru
                    );
                })
                .toList();
    }
}
