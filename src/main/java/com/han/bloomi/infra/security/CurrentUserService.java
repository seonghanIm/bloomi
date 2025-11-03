package com.han.bloomi.infra.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 현재 인증된 사용자 정보를 제공하는 서비스
 * SecurityContext에서 사용자 ID를 추출하는 공통 로직
 */
@Slf4j
@Service
public class CurrentUserService {

    /**
     * 현재 인증된 사용자의 ID 반환
     *
     * @return 사용자 ID (UUID)
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found in SecurityContext");
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        // JWT 인증의 경우 principal이 userId (String)
        if (principal instanceof String userId) {
            return userId;
        }

        // CustomOAuth2User의 경우
        if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUserId();
        }

        log.warn("Unexpected principal type: {}", principal.getClass().getName());
        throw new IllegalStateException("Unknown principal type: " + principal.getClass().getName());
    }

    /**
     * 현재 인증된 사용자의 ID를 Optional로 반환
     * 인증되지 않은 경우 empty 반환 (예외 발생 안 함)
     *
     * @return Optional<String> 사용자 ID
     */
    public java.util.Optional<String> getCurrentUserIdOptional() {
        try {
            return java.util.Optional.of(getCurrentUserId());
        } catch (IllegalStateException e) {
            return java.util.Optional.empty();
        }
    }

    /**
     * 현재 사용자가 인증되어 있는지 확인
     *
     * @return 인증 여부
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}