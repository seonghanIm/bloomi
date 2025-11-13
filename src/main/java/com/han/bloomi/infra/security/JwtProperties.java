package com.han.bloomi.infra.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bloomi.jwt")
public class JwtProperties {
    /**
     * JWT 서명에 사용할 비밀키
     */
    private String secret;

    /**
     * Access Token 유효시간 (밀리초)
     * 기본: 30일
     */
    private Long accessTokenValidity = 2592000000L;

    /**
     * Refresh Token 유효시간 (밀리초)
     * 기본: 30일
     */
    private Long refreshTokenValidity = 2592000000L;
}