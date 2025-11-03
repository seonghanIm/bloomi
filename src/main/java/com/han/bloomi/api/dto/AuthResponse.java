package com.han.bloomi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 인증 토큰 응답
 */
@Schema(description = "JWT 인증 토큰 응답")
@Builder
public record AuthResponse(
    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,

    @Schema(description = "JWT Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken,

    @Schema(description = "Access Token 만료 시간 (초)", example = "3600")
    Long expiresIn
) {
    public static AuthResponse of(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L) // 1시간 (초 단위)
                .build();
    }

    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}