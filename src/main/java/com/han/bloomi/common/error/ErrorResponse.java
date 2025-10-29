package com.han.bloomi.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    String traceId,
    String detail
) {
    public static ErrorResponse of(ErrorCode errorCode, String traceId) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .traceId(traceId)
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String traceId, String detail) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .traceId(traceId)
                .detail(detail)
                .build();
    }
}