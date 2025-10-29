package com.han.bloomi.api.dto;

public record AnalyzeMealRequest(
    String name,
    Double weight,
    String notes
) {
}