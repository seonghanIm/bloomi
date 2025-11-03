package com.han.bloomi.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/**
 * 표준 API 응답 구조
 * 모든 성공 응답에 사용
 *
 * @param <T> 응답 데이터 타입
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    String code,
    String message,
    T data,
    String traceId
) {
    /**
     * 성공 응답 (데이터 있음)
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message("Success")
                .data(data)
                .build();
    }

    /**
     * 성공 응답 (데이터 있음, 커스텀 메시지)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message(message)
                .build();
    }

    /**
     * 성공 응답 (커스텀 코드)
     */
    public static <T> ApiResponse<T> of(String code, String message, T data) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 성공 응답 (TraceId 포함)
     */
    public static <T> ApiResponse<T> of(String code, String message, T data, String traceId) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .traceId(traceId)
                .build();
    }
}
