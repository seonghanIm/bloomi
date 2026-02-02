package com.han.bloomi.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "식단 분석 결과 (AI 분석만, 저장 전)")
@Builder
public record AnalyzeMealResponse(
    @Schema(description = "S3 이미지 URL", example = "https://s3.../meals/...")
    String imageUrl,

    @Schema(description = "음식명 (AI 추정)", example = "유기농 사과")
    String name,

    @Schema(description = "총 칼로리 (kcal)", example = "120")
    double calories,

    @Schema(description = "3대 영양소 정보")
    Macros macros,

    @Schema(description = "제공량 정보")
    Serving serving,

    @Schema(description = "개별 음식 항목 목록")
    List<Item> items,

    @Schema(description = "분석 신뢰도 (0~1)", example = "0.85")
    double confidence,

    @Schema(description = "영양 조언", example = "비타민이 풍부해요")
    String advice,

    @Schema(description = "요청 추적 ID", example = "2b6f-a3c1")
    @JsonProperty("traceId") String traceId
) {
    @Schema(description = "3대 영양소 (탄수화물, 단백질, 지방)")
    @Builder
    public record Macros(
        @Schema(description = "탄수화물 (g)", example = "30")
        double carbs,

        @Schema(description = "단백질 (g)", example = "1")
        double protein,

        @Schema(description = "지방 (g)", example = "0.5")
        double fat
    ) {}

    @Schema(description = "제공량 정보")
    @Builder
    public record Serving(
        @Schema(description = "단위 (g 또는 ml)", example = "g")
        String unit,

        @Schema(description = "양", example = "100")
        double amount
    ) {}

    @Schema(description = "개별 음식 항목")
    @Builder
    public record Item(
        @Schema(description = "음식 이름", example = "사과")
        String name,

        @Schema(description = "양", example = "100")
        double amount,

        @Schema(description = "단위", example = "g")
        String unit,

        @Schema(description = "칼로리 (kcal)", example = "120")
        double calories
    ) {}
}