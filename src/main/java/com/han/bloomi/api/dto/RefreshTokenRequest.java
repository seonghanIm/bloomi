package com.han.bloomi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Refresh Token 요청 DTO
 */
@Schema(description = "토큰 갱신 요청")
public record RefreshTokenRequest(
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}