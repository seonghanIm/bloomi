package com.han.bloomi.domain.model;

import lombok.Builder;

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
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static User of(String id, String email, String name, String picture,
                          String provider, String providerId) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(id)
                .email(email)
                .name(name)
                .picture(picture)
                .provider(provider)
                .providerId(providerId)
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
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
