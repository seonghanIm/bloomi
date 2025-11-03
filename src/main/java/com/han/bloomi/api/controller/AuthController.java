package com.han.bloomi.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.han.bloomi.api.dto.AuthResponse;
import com.han.bloomi.api.dto.UserResponse;
import com.han.bloomi.common.response.ApiResponse;
import com.han.bloomi.common.swagger.ApiCommonResponses;
import com.han.bloomi.domain.model.user.User;
import com.han.bloomi.domain.port.UserRepository;
import com.han.bloomi.infra.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 인증 관련 컨트롤러
 */
@Tag(name = "Authentication", description = "사용자 인증 및 계정 관리 API")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "OAuth2 인증 콜백",
            description = "OAuth2 인증 후 JWT 토큰을 전달하는 엔드포인트입니다.",
            security = {}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "인증 성공",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
    )
    @GetMapping("/callback")
    public void authCallback(
            @Parameter(description = "JWT Access Token", required = true)
            @RequestParam String accessToken,

            @Parameter(description = "JWT Refresh Token", required = false)
            @RequestParam(required = false) String refreshToken,

            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        log.info("Auth callback received");

        // User-Agent로 모바일 여부 확인
        String userAgent = request.getHeader("User-Agent");
        boolean isMobile = userAgent != null && userAgent.toLowerCase().contains("mobile");

        log.info("User-Agent: {}, isMobile: {}", userAgent, isMobile);

        if (isMobile) {
            // 모바일: deep link로 리다이렉트
            // 토큰에서 사용자 정보 추출
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

            String mobileUrl = "bloomi://auth/callback?token=" + accessToken + "&user=" + encodedUser;
            log.info("📱 Redirecting to mobile app");
            response.sendRedirect(mobileUrl);
        } else {
            // 웹: JSON 응답
            log.info("🌐 Returning JSON response for web");
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"AUTH_SUCCESS\",\"message\":\"Authentication successful\",\"data\":{\"accessToken\":\"" + accessToken + "\",\"refreshToken\":\"" + refreshToken + "\"}}");
        }
    }

    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "JWT 토큰으로 인증된 사용자의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    @ApiCommonResponses.AuthenticatedApi
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        log.info("Get current user: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ApiResponse.success("User info retrieved", UserResponse.from(user));
    }

    @Operation(
            summary = "로그아웃",
            description = "사용자 로그아웃 처리 (클라이언트에서 토큰 삭제 필요)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공"
    )
    @ApiCommonResponses.AuthRequired
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        log.info("Logout request: userId={}", userId);
        // JWT는 stateless이므로 서버에서 할 일은 없음
        // 클라이언트에서 토큰을 삭제하면 됨
        return ApiResponse.success("Logout successful");
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "Soft Delete로 탈퇴 처리합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "탈퇴 성공"
    )
    @ApiCommonResponses.AuthenticatedApi
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        log.info("Delete user request: userId={}", userId);
        userRepository.delete(userId);
        log.info("User deleted successfully: userId={}", userId);
        return ApiResponse.success("User deleted successfully");
    }
}