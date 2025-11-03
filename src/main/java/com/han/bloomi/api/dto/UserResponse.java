package com.han.bloomi.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.han.bloomi.domain.model.user.Membership;
import com.han.bloomi.domain.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답
 */
@Schema(description = "사용자 정보")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
    @Schema(description = "사용자 ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    String id,

    @Schema(description = "이메일 주소", example = "user@example.com")
    String email,

    @Schema(description = "사용자 이름", example = "홍길동")
    String name,

    @Schema(description = "프로필 이미지 URL", example = "https://bloomi-images.s3.ap-northeast-2.amazonaws.com/profiles/...")
    String picture,

    @Schema(description = "OAuth 제공자", example = "google")
    String provider,

    @Schema(description = "멤버십 등급", example = "FREE")
    Membership membership,

    @Schema(description = "계정 생성일시", example = "2025-11-03T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "마지막 수정일시", example = "2025-11-03T15:45:00")
    LocalDateTime updatedAt
) {
    /**
     * 도메인 모델에서 DTO로 변환
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.id())
                .email(user.email())
                .name(user.name())
                .picture(user.picture())
                .provider(user.provider())
                .membership(user.membership())
                .createdAt(user.createdAt())
                .updatedAt(user.updatedAt())
                .build();
    }

    /**
     * 간단한 사용자 정보 (프로필용)
     */
    public static UserResponse simple(User user) {
        return UserResponse.builder()
                .id(user.id())
                .email(user.email())
                .name(user.name())
                .picture(user.picture())
                .membership(user.membership())
                .build();
    }
}
