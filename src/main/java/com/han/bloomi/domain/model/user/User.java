package com.han.bloomi.domain.model.user;

import lombok.Builder;

import java.lang.reflect.Member;
import java.time.LocalDateTime;

/**
 * 사용자 도메인 모델
 * OAuth2 인증을 통해 생성되는 사용자 정보
 */
@Builder
public record User(
    String id,              // 내부 사용자 ID (UUID)
    String email,           // 이메일 (Google에서 제공)
    String name,            // 이름
    String picture,         // 프로필 이미지 URL
    String provider,        // OAuth 제공자 (google, kakao 등)
    String providerId,      // Provider에서의 사용자 ID
    Membership membership,
    Integer dailyRequestCount,  // 일일 요청 횟수
    LocalDateTime lastRequestDate, // 마지막 요청 날짜
    Boolean deleted,        // 삭제 여부 (soft delete)
    LocalDateTime deletedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static User of(String id, String email, String name, String picture,
                          String provider, String providerId, Membership membership) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(id)
                .email(email)
                .name(name)
                .picture(picture)
                .provider(provider)
                .providerId(providerId)
                .membership(membership)
                .dailyRequestCount(0)
                .lastRequestDate(null)
                .deleted(false)
                .deletedAt(null)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public User update(String name, String picture) {
        return User.builder()
                .id(this.id)
                .email(this.email)
                .name(name)
                .picture(picture)
                .provider(this.provider)
                .providerId(this.providerId)
                .membership(this.membership)
                .dailyRequestCount(this.dailyRequestCount)
                .lastRequestDate(this.lastRequestDate)
                .deleted(this.deleted)
                .deletedAt(this.deletedAt)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public User delete() {
        return User.builder()
                .id(this.id)
                .email(this.email)
                .name(this.name)
                .picture(this.picture)
                .provider(this.provider)
                .providerId(this.providerId)
                .membership(this.membership)
                .dailyRequestCount(this.dailyRequestCount)
                .lastRequestDate(this.lastRequestDate)
                .deleted(true)
                .deletedAt(LocalDateTime.now())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 일일 요청 횟수를 증가시킵니다.
     * - 날짜가 변경되었으면 카운트를 1로 초기화
     * - 같은 날이면 카운트 증가
     */
    public User incrementDailyRequestCount() {
        LocalDateTime now = LocalDateTime.now();
        boolean isNewDay = this.lastRequestDate == null ||
                          !this.lastRequestDate.toLocalDate().equals(now.toLocalDate());

        return User.builder()
                .id(this.id)
                .email(this.email)
                .name(this.name)
                .picture(this.picture)
                .provider(this.provider)
                .providerId(this.providerId)
                .membership(this.membership)
                .dailyRequestCount(isNewDay ? 1 : this.dailyRequestCount + 1)
                .lastRequestDate(now)
                .deleted(this.deleted)
                .deletedAt(this.deletedAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 일일 요청 제한을 초과했는지 확인합니다.
     * FREE 멤버십: 하루 3회 제한
     */
    public boolean hasExceededDailyLimit() {
        if (this.membership == Membership.FREE) {
            LocalDateTime now = LocalDateTime.now();
            boolean isSameDay = this.lastRequestDate != null &&
                              this.lastRequestDate.toLocalDate().equals(now.toLocalDate());
            return isSameDay && this.dailyRequestCount >= 3;
        }
        return false; // PREMIUM은 제한 없음
    }
}
