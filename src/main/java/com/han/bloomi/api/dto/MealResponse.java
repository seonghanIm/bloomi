package com.han.bloomi.api.dto;

import com.han.bloomi.domain.model.MealEmotion;
import com.han.bloomi.domain.model.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "저장된 식단 정보 (AI 분석 + 사용자 메타데이터)")
@Builder
public record MealResponse(
    @Schema(description = "식단 기록 ID")
    String id,

    @Schema(description = "S3 이미지 URL")
    String imageUrl,

    @Schema(description = "음식명")
    String name,

    @Schema(description = "총 칼로리 (kcal)")
    double calories,

    @Schema(description = "3대 영양소")
    Macros macros,

    @Schema(description = "제공량")
    Serving serving,

    @Schema(description = "분석 신뢰도 (0~1)")
    double confidence,

    @Schema(description = "영양 조언")
    String advice,

    // 사용자 메타데이터
    @Schema(description = "식사 날짜")
    LocalDate analyzedAt,

    @Schema(description = "식사 타입")
    MealType mealType,

    @Schema(description = "감정 상태")
    MealEmotion emotion,

    @Schema(description = "추가 설명 (메모)")
    String notes,

    @Schema(description = "함께한 사람 목록")
    List<String> participants,

    @Schema(description = "식사 장소")
    String location,

    @Schema(description = "요청 추적 ID")
    String traceId
) {
    @Builder
    public record Macros(double carbs, double protein, double fat) {}

    @Builder
    public record Serving(String unit, double amount) {}
}