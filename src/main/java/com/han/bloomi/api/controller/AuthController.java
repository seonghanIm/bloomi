package com.han.bloomi.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 인증 관련 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * OAuth2 인증 콜백 엔드포인트
     * 프론트엔드로 토큰 전달
     */
    @GetMapping("/callback")
    public Map<String, String> authCallback(
            @RequestParam String accessToken,
            @RequestParam String refreshToken
    ) {
        log.info("Auth callback received");

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "message", "Authentication successful"
        );
    }

    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    public Map<String, String> getCurrentUser(
            @RequestParam(required = false) String userId
    ) {
        if (userId == null) {
            // SecurityContext에서 추출 필요
            return Map.of("message", "Not authenticated");
        }

        return Map.of(
                "userId", userId,
                "message", "User info retrieved"
        );
    }
}