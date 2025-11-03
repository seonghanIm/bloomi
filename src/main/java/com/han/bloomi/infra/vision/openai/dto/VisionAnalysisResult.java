package com.han.bloomi.infra.vision.openai.dto;

import lombok.Builder;

import java.util.List;

/**
 * OpenAI Vision API로부터 받은 JSON 응답을 매핑하는 DTO
 */
@Builder
public record VisionAnalysisResult(
    String name,
    Double calories,
    Macros macros,
    Serving serving,
    List<Item> items,
    Double confidence,
    String advice
) {
    @Builder
    public record Macros(
        Double carbs,
        Double protein,
        Double fat
    ) {}

    @Builder
    public record Serving(
        String unit,
        Double amount
    ) {}

    @Builder
    public record Item(
        String name,
        Double amount,
        String unit,
        Double calories
    ) {}
}