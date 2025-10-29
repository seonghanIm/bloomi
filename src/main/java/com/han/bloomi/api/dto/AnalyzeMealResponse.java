package com.han.bloomi.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record AnalyzeMealResponse(
    double calories,
    Macros macros,
    Serving serving,
    List<Item> items,
    double confidence,
    String advice,
    @JsonProperty("traceId") String traceId
) {
    @Builder
    public record Macros(
        double carbs,
        double protein,
        double fat
    ) {}

    @Builder
    public record Serving(
        String unit,
        double amount
    ) {}

    @Builder
    public record Item(
        String name,
        double amount,
        String unit,
        double calories
    ) {}
}
