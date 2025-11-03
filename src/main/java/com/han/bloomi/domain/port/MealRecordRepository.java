package com.han.bloomi.domain.port;

import com.han.bloomi.domain.model.MealRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 식단 기록 저장소 포트
 */
public interface MealRecordRepository {
    /**
     * 식단 기록 저장
     */
    MealRecord save(MealRecord record);

    /**
     * ID로 조회
     */
    Optional<MealRecord> findById(String id);

    /**
     * 사용자 ID로 전체 조회
     */
    List<MealRecord> findByUserId(String userId);

    /**
     * 사용자 ID와 날짜로 조회
     */
    List<MealRecord> findByUserIdAndAnalyzedAt(String userId, LocalDate analyzedAt);

    /**
     * 사용자 ID와 날짜 범위로 조회
     */
    List<MealRecord> findByUserIdAndAnalyzedAtBetween(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * 식단 기록 삭제
     */
    void deleteById(String id);
}