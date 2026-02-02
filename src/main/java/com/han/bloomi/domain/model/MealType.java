package com.han.bloomi.domain.model;

import java.time.LocalTime;

/**
 * 식사 유형
 */
public enum MealType {
    BREAKFAST,      // 아침
    BRUNCH,         // 아침 겸 점심
    LUNCH,          // 점심
    LINNER,         // 점심 겸 저녁
    DINNER,         // 저녁
    LATE_NIGHT,     // 야식
    SNACK,          // 간식
    COFFEE,         // 커피
    ALCOHOL;        // 술

    /**
     * 현재 시간 기반으로 기본 식사 유형 추정
     */
    public static MealType fromCurrentTime() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        if (hour >= 6 && hour < 10) {
            return BREAKFAST;
        } else if (hour >= 10 && hour < 12) {
            return BRUNCH;
        } else if (hour >= 12 && hour < 14) {
            return LUNCH;
        } else if (hour >= 14 && hour < 17) {
            return SNACK;
        } else if (hour >= 17 && hour < 21) {
            return DINNER;
        } else {
            return LATE_NIGHT;
        }
    }
}