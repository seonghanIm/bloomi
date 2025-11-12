package com.han.bloomi.api.controller;

import com.han.bloomi.api.dto.AuthResponse;
import com.han.bloomi.api.dto.UserResponse;
import com.han.bloomi.application.service.AuthService;
import com.han.bloomi.common.response.CustomApiResponse;
import com.han.bloomi.common.swagger.ApiCommonResponses;
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

/**
 * 인증 관련 컨트롤러
 */
@Tag(name = "Authentication", description = "사용자 인증 및 계정 관리 API")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

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
            log.info("📱 Redirecting to mobile app");
            String mobileUrl = authService.generateMobileDeepLink(accessToken);
            response.sendRedirect(mobileUrl);
        } else {
            // 웹: JSON 응답
            log.info("🌐 Returning JSON response for web");
            response.setContentType("application/json");
            String jsonResponse = authService.generateWebJsonResponse(accessToken, refreshToken);
            response.getWriter().write(jsonResponse);
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
    public CustomApiResponse<UserResponse> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        log.info("Get current user: userId={}", userId);
        return CustomApiResponse.success("User info retrieved", UserResponse.from(authService.getCurrentUser(userId)));
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
    public CustomApiResponse<Void> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        log.info("Logout request: userId={}", userId);
        authService.logout(userId);
        return CustomApiResponse.success("Logout successful");
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
    public CustomApiResponse<Void> deleteCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        log.info("Delete user request: userId={}", userId);
        authService.deleteUser(userId);
        return CustomApiResponse.success("User deleted successfully");
    }
}