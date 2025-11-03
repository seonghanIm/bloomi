package com.han.bloomi.domain.model;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 식단 기록 도메인 모델
 * Vision API 분석 결과 + 이미지 경로 + 사용자 정보
 */
@Builder
public record MealRecord(
    String id,                  // 기록 ID (UUID)
    String userId,              // 사용자 ID
    String imageUrl,            // S3 이미지 URL
    String name,                // 음식명 (AI 추정 또는 사용자 입력)
    double calories,            // 총 칼로리
    Macros macros,              // 3대 영양소
    Serving serving,            // 제공량
    double confidence,          // 확신도 (0~1)
    String advice,              // 조언
    String userInputName,       // 사용자가 입력한 음식명 (선택)
    Double userInputWeight,     // 사용자가 입력한 중량 (선택)
    String notes,               // 사용자 메모 (선택)
    LocalDate analyzedAt,       // 분석 날짜
    LocalDateTime createdAt
) {
    /**
     * Vision 분석 결과로부터 MealRecord 생성
     */
    public static MealRecord of(
            String id,
            String userId,
            String imageUrl,
            MealAnalysis analysis,
            String userInputName,
            Double userInputWeight,
            String notes
    ) {
        LocalDateTime now = LocalDateTime.now();
        return MealRecord.builder()
                .id(id)
                .userId(userId)
                .imageUrl(imageUrl)
                .name(analysis.name())
                .calories(analysis.calories())
                .macros(analysis.macros())
                .serving(analysis.serving())
                .confidence(analysis.confidence())
                .advice(analysis.advice())
                .userInputName(userInputName)
                .userInputWeight(userInputWeight)
                .notes(notes)
                .analyzedAt(now.toLocalDate())
                .createdAt(now)
                .build();
    }
}