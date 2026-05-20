package com.example.backend.service;

import com.example.backend.dto.SolvedQuestionCountDTO;
import com.example.backend.dto.SolvedQuestionsDTO;
import com.example.backend.model.Soru;
import com.example.backend.model.UserSolvedQuestion;
import com.example.backend.repository.SoruRepository;
import com.example.backend.repository.UserSolvedQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;

@Service
public class UserSolvedQuestionService {

    public static final String DURUM_BOS = "BOS";
    public static final String DURUM_DOGRU = "DOGRU";
    public static final String DURUM_YANLIS = "YANLIS";

    private final UserSolvedQuestionRepository solvedRepo;
    private final SoruRepository soruRepo;

    public UserSolvedQuestionService(UserSolvedQuestionRepository solvedRepo, SoruRepository soruRepo) {
        this.solvedRepo = solvedRepo;
        this.soruRepo = soruRepo;
    }

    @Transactional(readOnly = true)
    public Set<Long> getExcludedSoruIds(Long userId, Long dersId, Long konuId) {
        if (userId == null) return Set.of();
        if (konuId != null) return solvedRepo.findExcludedSoruIdsByUserIdAndKonuId(userId, konuId);
        if (dersId != null) return solvedRepo.findExcludedSoruIdsByUserIdAndDersId(userId, dersId);
        return solvedRepo.findExcludedSoruIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Set<Long> getBlankSoruIds(Long userId, Long dersId, Long konuId) {
        if (userId == null) return Set.of();
        if (konuId != null) return solvedRepo.findBlankSoruIdsByUserIdAndKonuId(userId, konuId);
        if (dersId != null) return solvedRepo.findBlankSoruIdsByUserIdAndDersId(userId, dersId);
        return solvedRepo.findBlankSoruIdsByUserId(userId);
    }

    /** Geriye uyumluluk */
    @Transactional(readOnly = true)
    public Set<Long> getSolvedSoruIds(Long userId, Long dersId, Long konuId) {
        return getExcludedSoruIds(userId, dersId, konuId);
    }

    @Transactional(readOnly = true)
    public SolvedQuestionsDTO listSolved(Long userId, Long dersId, Long konuId) {
        Set<Long> ids = getExcludedSoruIds(userId, dersId, konuId);
        return new SolvedQuestionsDTO(new ArrayList<>(ids), ids.size());
    }

    @Transactional(readOnly = true)
    public SolvedQuestionCountDTO countSolved(Long userId, Long dersId, Long konuId) {
        if (userId == null) return new SolvedQuestionCountDTO(0);
        long count;
        if (konuId != null) {
            count = solvedRepo.countAnsweredByUserIdAndKonuId(userId, konuId);
        } else if (dersId != null) {
            count = solvedRepo.countAnsweredByUserIdAndDersId(userId, dersId);
        } else {
            count = solvedRepo.countAnsweredByUserId(userId);
        }
        return new SolvedQuestionCountDTO(count);
    }

    @Transactional
    public void markAttempt(Long userId, Long soruId, boolean bosmu, boolean dogru, Long oturumId) {
        if (userId == null || soruId == null) return;

        String sonDurum = bosmu ? DURUM_BOS : (dogru ? DURUM_DOGRU : DURUM_YANLIS);

        Soru soru = soruRepo.findById(soruId)
                .orElseThrow(() -> new IllegalArgumentException("Soru bulunamadi: " + soruId));

        UserSolvedQuestion row = solvedRepo.findByUserIdAndSoru_Id(userId, soruId)
                .orElseGet(() -> {
                    UserSolvedQuestion created = new UserSolvedQuestion();
                    created.setUserId(userId);
                    created.setSoru(soru);
                    return created;
                });

        row.setCozuldu(!DURUM_BOS.equals(sonDurum));
        row.setCozulduAt(Instant.now());
        row.setDogru(bosmu ? null : dogru);
        row.setSonDurum(sonDurum);
        row.setOturumId(oturumId);
        solvedRepo.save(row);
    }

    /** @deprecated use markAttempt */
    @Transactional
    public void markSolved(Long userId, Long soruId, boolean dogru, Long oturumId) {
        markAttempt(userId, soruId, false, dogru, oturumId);
    }
}
