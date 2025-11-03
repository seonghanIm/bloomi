package com.han.bloomi.domain.model.user;

import lombok.Getter;

@Getter
public enum Membership {
    FREE("FREE"), // 일 2회 무료, 광고 노출, 광고 3회 시청 시 1회 적립
    TIER1("TIER1"), // 일 5회 무료, 광고 노출, 광고 3회 노출 시 1회 적립
    TIER2("TIER2"), // 일 5회 무료, 광고 노출 x, 광고 3회 노출 시 1회 적립
    TIER3("TIER3"), // 월 300 회 내에서 무료(수정 가능), 광고 노출 x
    ;

    private final String value;

    Membership(String value) {
        this.value = value;
    }
}
