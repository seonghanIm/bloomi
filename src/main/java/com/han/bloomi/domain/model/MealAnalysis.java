package com.han.bloomi.domain.model;

import lombok.Builder;

import java.util.List;

/**
 * 식사 분석 결과를 표현하는 도메인 모델
 * Vision API로부터 받은 원시 데이터를 도메인 객체로 변환한 결과
 */
@Builder
public record MealAnalysis(
    double calories,
    Macros macros,
    Serving serving,
    List<FoodItem> items,
    double confidence,
    String advice
) {
    public static MealAnalysis of(
            double calories,
            Macros macros,
            Serving serving,
            List<FoodItem> items,
            double confidence,
            String advice
    ) {
        return MealAnalysis.builder()
                .calories(calories)
                .macros(macros)
                .serving(serving)
                .items(items)
                .confidence(confidence)
                .advice(advice)
                .build();
    }
}