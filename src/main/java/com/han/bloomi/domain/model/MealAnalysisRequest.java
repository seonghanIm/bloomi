package com.han.bloomi.domain.model;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

/**
 * Vision API에 전달할 식사 분석 요청 정보
 */
@Builder
public record MealAnalysisRequest(
    MultipartFile image,
    String name,
    Double weight,
    String notes
) {
    public static MealAnalysisRequest of(MultipartFile image, String name, Double weight, String notes) {
        return MealAnalysisRequest.builder()
                .image(image)
                .name(name)
                .weight(weight)
                .notes(notes)
                .build();
    }

    public boolean hasHint() {
        return name != null || weight != null || notes != null;
    }

    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasWeight() {
        return weight != null && weight > 0;
    }
}