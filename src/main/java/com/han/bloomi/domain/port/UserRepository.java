package com.han.bloomi.domain.port;

import com.han.bloomi.domain.model.User;

import java.util.Optional;

/**
 * 사용자 저장소 포트
 * DB 없이 In-Memory 또는 향후 JPA로 구현
 */
public interface UserRepository {
    /**
     * 사용자 저장 (생성 또는 업데이트)
     */
    User save(User user);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * Provider와 ProviderId로 사용자 조회
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    /**
     * ID로 사용자 조회
     */
    Optional<User> findById(String id);
}
