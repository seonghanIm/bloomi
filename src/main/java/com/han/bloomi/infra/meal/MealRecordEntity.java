package com.han.bloomi.infra.meal;

import com.han.bloomi.infra.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 식단 기록 엔티티
 */
@Entity
@Table(name = "meal_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MealRecordEntity {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double calories;

    // 3대 영양소 (탄수화물, 단백질, 지방)
    @Column(nullable = false)
    private Double carbs;

    @Column(nullable = false)
    private Double protein;

    @Column(nullable = false)
    private Double fat;

    // 제공량
    @Column(nullable = false)
    private String servingUnit;

    @Column(nullable = false)
    private Double servingAmount;

    // 확신도 및 조언
    @Column(nullable = false)
    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String advice;

    // 사용자 입력 정보 (선택)
    private String userInputName;
    private Double userInputWeight;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDate analyzedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MealRecordEntity(String id, UserEntity user, String imageUrl, String name, Double calories,
                            Double carbs, Double protein, Double fat,
                            String servingUnit, Double servingAmount,
                            Double confidence, String advice,
                            String userInputName, Double userInputWeight, String notes,
                            LocalDate analyzedAt) {
        this.id = id;
        this.user = user;
        this.imageUrl = imageUrl;
        this.name = name;
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.servingUnit = servingUnit;
        this.servingAmount = servingAmount;
        this.confidence = confidence;
        this.advice = advice;
        this.userInputName = userInputName;
        this.userInputWeight = userInputWeight;
        this.notes = notes;
        this.analyzedAt = analyzedAt;
    }
}