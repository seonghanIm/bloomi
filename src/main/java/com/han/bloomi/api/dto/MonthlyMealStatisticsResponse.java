package com.han.bloomi.api.dto;

import com.han.bloomi.domain.model.MealEmotion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
@Schema(description = "월별 식단 통계 응답")
public record MonthlyMealStatisticsResponse(
        @Schema(description = "년-월", example = "2025-01")
        String yearMonth,

        @Schema(description = "날짜별 식단 기록 건수 (날짜 → 건수)")
        Map<LocalDate, Long> dailyCounts,

        @Schema(description = "날짜별 대표 감정 (날짜 → 감정). 해당 날짜의 가장 최근 식사 감정을 사용")
        Map<LocalDate, MealEmotion> dailyEmotions,

        @Schema(description = "총 기록 건수", example = "45")
        long totalCount,

        @Schema(description = "traceId")
        String traceId
) {
}