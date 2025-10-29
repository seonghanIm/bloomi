package com.han.bloomi.common.error;

import com.han.bloomi.common.exception.BusinessException;
import com.han.bloomi.common.trace.TraceIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final TraceIdHolder traceIdHolder;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String traceId = traceIdHolder.current();
        log.error("[{}] BusinessException: {} - {}", traceId, ex.getErrorCode().getCode(), ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), traceId, ex.getDetail());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String traceId = traceIdHolder.current();
        log.warn("[{}] File size exceeded: {}", traceId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.PAYLOAD_TOO_LARGE, traceId);
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(
            MissingServletRequestPartException ex, HttpServletRequest request) {
        String traceId = traceIdHolder.current();
        log.warn("[{}] Missing required part: {}", traceId, ex.getRequestPartName());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.MISSING_REQUIRED_FIELD,
                traceId,
                "Missing required part: " + ex.getRequestPartName()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = traceIdHolder.current();
        log.warn("[{}] Validation failed: {}", traceId, ex.getMessage());

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST, traceId, detail);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        String traceId = traceIdHolder.current();
        log.error("[{}] Unhandled exception: {}", traceId, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, traceId);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}