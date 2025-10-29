package com.han.bloomi.domain.model;

import lombok.Builder;

@Builder
public record Macros(
    double carbs,
    double protein,
    double fat
) {
    public static Macros of(double carbs, double protein, double fat) {
        return Macros.builder()
                .carbs(carbs)
                .protein(protein)
                .fat(fat)
                .build();
    }
}