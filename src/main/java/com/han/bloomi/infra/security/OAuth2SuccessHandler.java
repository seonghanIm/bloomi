package com.han.bloomi.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.han.bloomi.api.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * OAuth2 인증 성공 시 처리
 * JWT 토큰을 생성하여 프론트엔드로 리다이렉트
 * 모바일 앱(deep link)과 웹 모두 지원
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        log.info("OAuth2 authentication successful for user: {}", oAuth2User.getEmail());

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                oAuth2User.getUserId(),
                oAuth2User.getEmail()
        );

        log.info("JWT tokens generated for user: {}", oAuth2User.getUserId());

        // state 파라미터로 모바일 여부 확인
        String state = request.getParameter("state");
        log.info("OAuth callback - state: {}", state);

        // Refresh Token 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(oAuth2User.getUserId());

        // state에 "mobile"이 포함되어 있으면 모바일로 간주
        if (state != null && state.contains("mobile")) {
            // 모바일 앱으로 리다이렉트
            handleMobileRedirect(response, accessToken, refreshToken, oAuth2User, "bloomi://auth/callback");
        } else {
            // 웹 클라이언트로 리다이렉트 (기존 로직)
            handleWebRedirect(request, response, accessToken, refreshToken);
        }
    }

    /**
     * 모바일 앱으로 deep link 리다이렉트
     */
    private void handleMobileRedirect(HttpServletResponse response,
                                       String accessToken,
                                       String refreshToken,
                                       CustomOAuth2User oAuth2User,
                                       String redirectUri) throws IOException {
        // 사용자 정보 JSON 생성 (모바일 앱에서 필요한 필드만)
        Map<String, Object> userInfo = Map.of(
                "id", oAuth2User.getUserId(),
                "email", oAuth2User.getEmail(),
                "name", oAuth2User.getName(),
                "picture", oAuth2User.getPicture() != null ? oAuth2User.getPicture() : "",
                "provider", oAuth2User.getProvider(),
                "membership", oAuth2User.getMembership().name(),
                "isNewUser", oAuth2User.isNewUser(),
                "termsAgreed", oAuth2User.isTermsAgreed()
        );

        String userJson = objectMapper.writeValueAsString(userInfo);
        String encodedUser = URLEncoder.encode(userJson, StandardCharsets.UTF_8);

        // 모바일 deep link URL 생성
        String mobileUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("user", encodedUser)
                .build()
                .toUriString();

        log.info("📱 Redirecting to mobile app: {}, isNewUser={}, termsAgreed={}",
                redirectUri, oAuth2User.isNewUser(), oAuth2User.isTermsAgreed());
        getRedirectStrategy().sendRedirect(null, response, mobileUrl);
    }

    /**
     * 웹 클라이언트로 리다이렉트 (기존 로직)
     */
    private void handleWebRedirect(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String accessToken,
                                    String refreshToken) throws IOException {
        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        // TODO: 프로덕션에서는 더 안전한 방법 사용 (쿠키, POST 등)
        String targetUrl = UriComponentsBuilder.fromUriString("/auth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        log.info("🌐 Redirecting to web client: /auth/callback");
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}