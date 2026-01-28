package com.han.bloomi.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 식사 타입
 */
@Getter
@RequiredArgsConstructor
public enum MealType {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    SNACK("간식");

    private final String displayName;

    /**
     * 현재 시간에 따라 기본 식사 타입 결정
     */
    public static MealType fromCurrentTime() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour >= 5 && hour < 10) {
            return BREAKFAST;
        } else if (hour >= 10 && hour < 15) {
            return LUNCH;
        } else if (hour >= 15 && hour < 21) {
            return DINNER;
        } else {
            return SNACK;
        }
    }
}