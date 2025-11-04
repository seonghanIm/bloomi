package com.han.bloomi.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "Invalid input provided"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "MISSING_FIELD", "Required field is missing"),
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", "Invalid image format"),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"),

    // 413 Payload Too Large
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE", "Image size exceeds limit"),

    // 415 Unsupported Media Type
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA", "Unsupported media type"),

    // 429 Too Many Requests
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "Rate limit exceeded"),
    DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "DAILY_LIMIT_EXCEEDED", "일일 사용 횟수를 초과했습니다. Bloomi는 일 3회의 무료 식단 분석을 제공합니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error"),

    // 502 Bad Gateway
    VISION_API_ERROR(HttpStatus.BAD_GATEWAY, "VISION_API_ERROR", "Vision API error occurred"),
    VISION_TIMEOUT(HttpStatus.BAD_GATEWAY, "VISION_TIMEOUT", "Provider timeout"),
    VISION_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "VISION_INVALID_RESPONSE", "Invalid response from vision provider"),
    VISION_NO_MEAL(HttpStatus.BAD_REQUEST, "VISION_NO_MEAL", "음식 사진이 아닙니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}