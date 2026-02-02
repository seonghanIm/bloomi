package com.han.bloomi.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 행복도 (5단계)
 */
@Getter
@RequiredArgsConstructor
public enum MealEmotion {
    TERRIBLE(1, "죽겠어요"),       // 1단계
    HARD(2, "조금 힘들어요"),       // 2단계
    OKAY(3, "할만해요"),           // 3단계
    ENJOYABLE(4, "즐거워요"),      // 4단계
    VERY_HAPPY(5, "아주 행복해요"); // 5단계

    private final int level;
    private final String displayName;
}