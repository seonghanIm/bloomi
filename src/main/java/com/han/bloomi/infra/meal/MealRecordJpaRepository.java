package com.han.bloomi.infra.meal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 식단 기록 JPA Repository
 */
public interface MealRecordJpaRepository extends JpaRepository<MealRecordEntity, String> {
    List<MealRecordEntity> findByUser_Id(String userId);
    List<MealRecordEntity> findByUser_IdAndAnalyzedAt(String userId, LocalDate analyzedAt);
    List<MealRecordEntity> findByUser_IdAndAnalyzedAtBetween(String userId, LocalDate startDate, LocalDate endDate);
}