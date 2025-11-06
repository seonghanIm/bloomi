package com.han.bloomi.domain.model.user;

import lombok.Getter;

@Getter
public enum Membership {
    FREE("FREE", 3), // 일 2회 무료, 광고 노출, 광고 3회 시청 시 1회 적립
    TIER1("TIER1", 5), // 일 5회 무료, 광고 노출, 광고 3회 노출 시 1회 적립
    TIER2("TIER2",5), // 일 5회 무료, 광고 노출 x, 광고 3회 노출 시 1회 적립
    TIER3("TIER3",30), // 일 30 회 내에서 무료(수정 가능), 광고 노출 x
    ;

    private final String value;
    private final int dailyLimit;

    Membership(String value, int dailyLimit) {
        this.value = value;
        this.dailyLimit = dailyLimit;
    }
}
