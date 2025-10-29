package com.han.bloomi.infra.security;

import com.han.bloomi.domain.model.User;
import com.han.bloomi.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * OAuth2 사용자 정보를 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("OAuth2 login from provider: {}", registrationId);

        // Google OAuth2 정보 추출
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String providerId = (String) attributes.get("sub");

        // 사용자 조회 또는 생성
        User user = userRepository.findByProviderAndProviderId(registrationId, providerId)
                .map(existingUser -> {
                    // 기존 사용자 정보 업데이트
                    log.info("Existing user found: {}", existingUser.email());
                    return existingUser.update(name, picture);
                })
                .orElseGet(() -> {
                    // 새 사용자 생성
                    log.info("Creating new user: {}", email);
                    return User.of(
                            UUID.randomUUID().toString(),
                            email,
                            name,
                            picture,
                            registrationId,
                            providerId
                    );
                });

        // 사용자 저장
        user = userRepository.save(user);

        log.info("User authenticated: id={}, email={}", user.id(), user.email());

        return new CustomOAuth2User(
                user.id(),
                user.email(),
                user.name(),
                user.picture(),
                attributes
        );
    }
}