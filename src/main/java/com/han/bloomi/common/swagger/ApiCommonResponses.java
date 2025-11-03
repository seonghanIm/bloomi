package com.han.bloomi.common.swagger;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Swagger 공통 응답 어노테이션 모음
 * 반복되는 응답 패턴을 재사용 가능하게 함
 */
public class ApiCommonResponses {

    /**
     * JWT 인증이 필요한 API의 기본 응답
     * 401 Unauthorized
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (JWT 토큰 누락 또는 만료)"
            )
    })
    public @interface AuthRequired {
    }

    /**
     * 멀티파트 파일 업로드 API의 공통 에러 응답
     * 400, 413, 415
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (파일 누락, 형식 오류 등)"
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "파일 크기 초과 (최대 5MB)"
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "지원하지 않는 미디어 타입"
            )
    })
    public @interface FileUploadErrors {
    }

    /**
     * 리소스 조회 실패 응답
     * 404 Not Found
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스를 찾을 수 없음"
            )
    })
    public @interface NotFound {
    }

    /**
     * 외부 API 연동 실패 응답
     * 502 Bad Gateway
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "502",
                    description = "외부 API 오류 (타임아웃 또는 응답 오류)"
            )
    })
    public @interface ExternalApiError {
    }

    /**
     * 인증이 필요한 API의 전체 공통 응답
     * 401 + 404
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @AuthRequired
    @NotFound
    public @interface AuthenticatedApi {
    }

    /**
     * 파일 업로드 + 인증이 필요한 API
     * 401 + 400 + 413 + 415
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @AuthRequired
    @FileUploadErrors
    public @interface AuthenticatedFileUpload {
    }
}