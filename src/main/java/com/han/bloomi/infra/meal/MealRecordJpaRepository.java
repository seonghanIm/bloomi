package com.han.bloomi.infra.meal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 식단 기록 JPA Repository
 */
public interface MealRecordJpaRepository extends JpaRepository<MealRecordEntity, String> {

    @Query("SELECT DISTINCT m FROM MealRecordEntity m LEFT JOIN FETCH m.participants WHERE m.id = :id")
    Optional<MealRecordEntity> findByIdWithParticipants(@Param("id") String id);

    @Query("SELECT DISTINCT m FROM MealRecordEntity m LEFT JOIN FETCH m.participants WHERE m.user.id = :userId")
    List<MealRecordEntity> findByUser_Id(@Param("userId") String userId);

    @Query("SELECT DISTINCT m FROM MealRecordEntity m LEFT JOIN FETCH m.participants WHERE m.user.id = :userId AND m.analyzedAt = :analyzedAt")
    List<MealRecordEntity> findByUser_IdAndAnalyzedAt(@Param("userId") String userId, @Param("analyzedAt") LocalDate analyzedAt);

    @Query("SELECT DISTINCT m FROM MealRecordEntity m LEFT JOIN FETCH m.participants WHERE m.user.id = :userId AND m.analyzedAt BETWEEN :startDate AND :endDate")
    List<MealRecordEntity> findByUser_IdAndAnalyzedAtBetween(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}