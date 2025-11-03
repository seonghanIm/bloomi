package com.han.bloomi.infra.security;

import com.han.bloomi.domain.model.user.Membership;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * OAuth2 인증 후 사용자 정보를 담는 클래스
 */
@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final String userId;
    private final String email;
    private final String name;
    private final String picture;
    private final String provider;
    private final Membership membership;
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return name;
    }
}