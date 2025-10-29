package com.han.bloomi.infra.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 인증 성공 시 처리
 * JWT 토큰을 생성하여 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;

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
        String refreshToken = jwtTokenProvider.createRefreshToken(oAuth2User.getUserId());

        log.info("JWT tokens generated for user: {}", oAuth2User.getUserId());

        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        // TODO: 프로덕션에서는 더 안전한 방법 사용 (쿠키, POST 등)
        String targetUrl = UriComponentsBuilder.fromUriString("/auth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}