package com.han.bloomi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 약관 동의 요청 DTO
 */
@Schema(description = "약관 동의 요청")
public record TermsAgreementRequest(
    @Schema(description = "서비스 이용 약관 동의 (필수)", example = "true", required = true)
    @NotNull(message = "서비스 이용 약관 동의는 필수입니다.")
    Boolean termsAgreed,

    @Schema(description = "개인정보 수집 및 이용 동의 (필수)", example = "true", required = true)
    @NotNull(message = "개인정보 수집 동의는 필수입니다.")
    Boolean privacyAgreed,

    @Schema(description = "마케팅 정보 수신 동의 (선택)", example = "false")
    Boolean marketingAgreed
) {
    public TermsAgreementRequest {
        // marketingAgreed가 null이면 false로 처리
        if (marketingAgreed == null) {
            marketingAgreed = false;
        }
    }
}
