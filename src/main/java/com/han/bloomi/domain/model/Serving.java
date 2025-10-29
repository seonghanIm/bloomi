package com.han.bloomi.domain.model;

import lombok.Builder;

@Builder
public record Serving(
    String unit,
    double amount
) {
    public static Serving of(String unit, double amount) {
        return Serving.builder()
                .unit(unit)
                .amount(amount)
                .build();
    }
}