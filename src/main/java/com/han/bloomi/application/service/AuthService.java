package com.han.bloomi.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.han.bloomi.api.dto.AuthResponse;
import com.han.bloomi.common.error.ErrorCode;
import com.han.bloomi.common.exception.BusinessException;
import com.han.bloomi.domain.model.user.AgeRange;
import com.han.bloomi.domain.model.user.Gender;
import com.han.bloomi.domain.model.user.User;
import com.han.bloomi.domain.port.UserRepository;
import com.han.bloomi.infra.security.JwtProperties;
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
    private final JwtProperties jwtProperties;
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

    /**
     * 약관 동의 처리
     */
    @Transactional
    public User agreeToTerms(String userId, Boolean termsAgreed, Boolean privacyAgreed, Boolean marketingAgreed) {
        log.info("Processing terms agreement for userId={}", userId);

        // 필수 약관 동의 검증
        if (!Boolean.TRUE.equals(termsAgreed) || !Boolean.TRUE.equals(privacyAgreed)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "필수 약관에 동의해야 합니다.");
        }

        User user = userRepository.agreeToTerms(userId, termsAgreed, privacyAgreed, marketingAgreed);
        log.info("Terms agreement completed for userId={}", userId);
        return user;
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    public AuthResponse refreshAccessToken(String refreshToken) {
        log.info("Token refresh requested");

        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Invalid refresh token");
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. Refresh Token 타입 검증
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            log.warn("Token is not a refresh token");
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Not a refresh token");
        }

        // 3. 사용자 존재 여부 확인
        String userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 4. 새로운 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user.id(), user.email());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.id());

        log.info("Token refreshed successfully for userId={}", userId);

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtProperties.getAccessTokenValidity() / 1000  // 밀리초 → 초
        );
    }

    /**
     * 닉네임 중복 확인
     * @param nickname 확인할 닉네임
     * @return 사용 가능하면 true
     */
    public boolean isNicknameAvailable(String nickname) {
        log.info("Checking nickname availability: {}", nickname);
        return !userRepository.existsByNickname(nickname);
    }

    /**
     * 온보딩 완료 처리
     */
    @Transactional
    public User completeOnboarding(String userId, String nickname, Gender gender, AgeRange ageRange) {
        log.info("Processing onboarding for userId={}, nickname={}", userId, nickname);

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 사용 중인 닉네임입니다.");
        }

        // 닉네임 형식 검증 (2~12자, 한글/영문만)
        if (!isValidNickname(nickname)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "닉네임은 2~12자의 한글 또는 영문만 사용 가능합니다.");
        }

        User user = userRepository.completeOnboarding(userId, nickname, gender, ageRange);
        log.info("Onboarding completed for userId={}", userId);
        return user;
    }

    /**
     * 닉네임 형식 검증
     */
    private boolean isValidNickname(String nickname) {
        if (nickname == null || nickname.length() < 2 || nickname.length() > 12) {
            return false;
        }
        // 한글(가-힣), 영문(a-zA-Z)만 허용
        return nickname.matches("^[가-힣a-zA-Z]+$");
    }
}