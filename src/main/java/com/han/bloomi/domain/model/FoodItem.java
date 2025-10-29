package com.han.bloomi.domain.model;

import lombok.Builder;

@Builder
public record FoodItem(
    String name,
    double amount,
    String unit,
    double calories
) {
    public static FoodItem of(String name, double amount, String unit, double calories) {
        return FoodItem.builder()
                .name(name)
                .amount(amount)
                .unit(unit)
                .calories(calories)
                .build();
    }
}