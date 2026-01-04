package com.han.bloomi.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * OAuth2 인증 요청 시 모바일 클라이언트 여부를 세션에 저장하는 필터
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MobileClientFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // OAuth2 인증 요청인 경우 client_type 파라미터 확인
        String requestUri = request.getRequestURI();
        if (requestUri != null && requestUri.contains("/oauth2/authorization/")) {
            String clientType = request.getParameter("client_type");
            log.info("OAuth2 authorization request - client_type: {}", clientType);

            if ("mobile".equals(clientType)) {
                request.getSession().setAttribute("mobile_client", "true");
                log.info("Mobile client detected, session attribute set");
            }
        }

        filterChain.doFilter(request, response);
    }
}
