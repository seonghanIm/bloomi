package com.han.bloomi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 닉네임 중복 확인 응답
 */
@Schema(description = "닉네임 중복 확인 응답")
public record NicknameCheckResponse(
    @Schema(description = "사용 가능 여부 (true: 사용 가능, false: 이미 사용 중)", example = "true")
    boolean available,

    @Schema(description = "확인 메시지", example = "사용 가능한 닉네임입니다.")
    String message
) {
    public static NicknameCheckResponse ofAvailable() {
        return new NicknameCheckResponse(true, "사용 가능한 닉네임입니다.");
    }

    public static NicknameCheckResponse ofNotAvailable() {
        return new NicknameCheckResponse(false, "이미 사용 중인 닉네임입니다.");
    }
}
