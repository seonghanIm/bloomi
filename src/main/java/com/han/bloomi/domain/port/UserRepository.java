package com.han.bloomi.domain.port;

import com.han.bloomi.domain.model.user.User;

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

    /**
     * 사용자 삭제 (Soft Delete)
     * @param id 사용자 ID
     * @return 삭제된 사용자
     */
    User delete(String id);

    /**
     * 일일 요청 카운트를 증가시킵니다.
     * @param userId 사용자 ID
     * @return 업데이트된 사용자
     */
    User incrementDailyRequestCount(String userId);
}
