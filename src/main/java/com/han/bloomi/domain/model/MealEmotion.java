package com.han.bloomi.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 식사 감정 상태
 */
@Getter
@RequiredArgsConstructor
public enum MealEmotion {
    HAPPY("행복해요", "😊"),
    SATISFIED("만족해요", "😋"),
    NORMAL("보통이에요", "😐"),
    SAD("슬퍼요", "😢"),
    STRESSED("스트레스", "😤"),
    TIRED("피곤해요", "😴");

    private final String displayName;
    private final String emoji;
}