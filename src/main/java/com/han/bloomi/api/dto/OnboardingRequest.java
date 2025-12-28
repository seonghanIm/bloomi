package com.han.bloomi.api.dto;

import com.han.bloomi.domain.model.user.AgeRange;
import com.han.bloomi.domain.model.user.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 온보딩 정보 요청
 */
@Schema(description = "온보딩 정보 요청")
public record OnboardingRequest(
    @Schema(description = "닉네임 (2~12자, 한글/영문만 허용)", example = "잇피")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.")
    String nickname,

    @Schema(description = "성별", example = "MALE")
    @NotNull(message = "성별은 필수입니다.")
    Gender gender,

    @Schema(description = "연령대", example = "TWENTIES")
    @NotNull(message = "연령대는 필수입니다.")
    AgeRange ageRange
) {}