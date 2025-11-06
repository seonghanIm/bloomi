package com.han.bloomi.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.han.bloomi.domain.model.user.User;
import com.han.bloomi.domain.port.UserRepository;
import com.han.bloomi.infra.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    /**
     * 모바일 딥링크 URL 생성
     */
    public String generateMobileDeepLink(String accessToken) {
        try {
            String userId = jwtTokenProvider.getUserId(accessToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Map<String, Object> userInfo = Map.of(
                    "id", user.id(),
                    "email", user.email(),
                    "name", user.name(),
                    "picture", user.picture() != null ? user.picture() : "",
                    "provider", user.provider(),
                    "membership", user.membership().name()
            );

            String userJson = objectMapper.writeValueAsString(userInfo);
            String encodedUser = URLEncoder.encode(userJson, StandardCharsets.UTF_8);

            return "bloomi://auth/callback?token=" + accessToken + "&user=" + encodedUser;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user info", e);
            throw new RuntimeException("Failed to generate mobile deep link", e);
        }
    }

    /**
     * 웹 JSON 응답 생성
     */
    public String generateWebJsonResponse(String accessToken, String refreshToken) {
        return "{\"code\":\"AUTH_SUCCESS\",\"message\":\"Authentication successful\",\"data\":{\"accessToken\":\""
                + accessToken + "\",\"refreshToken\":\"" + refreshToken + "\"}}";
    }

    /**
     * 사용자 정보 조회
     */
    public User getCurrentUser(String userId) {
        log.info("Fetching user info: userId={}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    /**
     * 로그아웃 처리
     * (현재는 클라이언트 측에서 토큰 삭제로 처리, 추후 토큰 블랙리스트 등 구현 가능)
     */
    public void logout(String userId) {
        log.info("Logout processing for userId={}", userId);
        // TODO: 토큰 블랙리스트 추가 등의 로직 구현 가능
    }

    /**
     * 회원 탈퇴 (Soft Delete)
     */
    @Transactional
    public void deleteUser(String userId) {
        log.info("Deleting user: userId={}", userId);
        userRepository.delete(userId);
        log.info("User deleted successfully: userId={}", userId);
    }
}