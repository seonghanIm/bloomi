package com.han.bloomi.api.dto;

import com.han.bloomi.domain.model.MealEmotion;
import com.han.bloomi.domain.model.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "식단 저장 요청 (분석 결과 + 사용자 메타데이터)")
public record SaveMealRequest(
    // === AI 분석 결과 (1단계에서 받은 값) ===
    @Schema(description = "S3 이미지 URL", example = "https://s3.../meals/...")
    @NotBlank(message = "이미지 URL은 필수입니다")
    String imageUrl,

    @Schema(description = "음식명", example = "유기농 사과")
    @NotBlank(message = "음식명은 필수입니다")
    String name,

    @Schema(description = "총 칼로리 (kcal)", example = "120")
    @NotNull(message = "칼로리는 필수입니다")
    @Positive(message = "칼로리는 양수여야 합니다")
    Double calories,

    @Schema(description = "탄수화물 (g)", example = "30")
    @NotNull Double carbs,

    @Schema(description = "단백질 (g)", example = "1")
    @NotNull Double protein,

    @Schema(description = "지방 (g)", example = "0.5")
    @NotNull Double fat,

    @Schema(description = "제공량 단위", example = "g")
    @NotBlank String servingUnit,

    @Schema(description = "제공량", example = "100")
    @NotNull @Positive Double servingAmount,

    @Schema(description = "분석 신뢰도 (0~1)", example = "0.85")
    Double confidence,

    @Schema(description = "영양 조언", example = "비타민이 풍부해요")
    String advice,

    // === 사용자 입력 메타데이터 (2단계에서 입력) ===
    @Schema(description = "식사 날짜", example = "2025-06-15")
    @NotNull(message = "날짜는 필수입니다")
    LocalDate analyzedAt,

    @Schema(description = "식사 타입", example = "BREAKFAST")
    MealType mealType,

    @Schema(description = "감정 상태", example = "HAPPY")
    MealEmotion emotion,

    @Schema(description = "추가 설명 (메모)", example = "아침에 먹은 사과, 맛있었다")
    String notes,

    @Schema(description = "함께한 사람 목록", example = "[\"나\", \"천지동\", \"김정윤\"]")
    List<String> participants,

    @Schema(description = "식사 장소", example = "화곡동 서연이네")
    String location
) {}