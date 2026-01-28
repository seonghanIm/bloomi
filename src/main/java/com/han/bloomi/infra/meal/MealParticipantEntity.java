package com.han.bloomi.infra.meal;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 식사 참여자 엔티티
 * meal_records와 1:N 관계
 */
@Entity
@Table(name = "meal_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MealParticipantEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_record_id", nullable = false)
    private MealRecordEntity mealRecord;

    @Column(nullable = false, length = 50)
    private String name;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MealParticipantEntity(String id, MealRecordEntity mealRecord, String name) {
        this.id = id;
        this.mealRecord = mealRecord;
        this.name = name;
    }

    /**
     * 연관관계 설정용 메서드
     */
    public void setMealRecord(MealRecordEntity mealRecord) {
        this.mealRecord = mealRecord;
    }
}