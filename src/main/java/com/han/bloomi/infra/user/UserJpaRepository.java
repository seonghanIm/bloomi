package com.han.bloomi.infra.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByProviderAndProviderId(String provider, String providerId);

    /**
     * 모든 사용자의 일일 요청 카운트를 0으로 리셋합니다.
     * 스케줄러에서 자정에 호출됩니다.
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.dailyRequestCount = 0 WHERE u.dailyRequestCount > 0")
    void resetAllDailyRequestCounts();
}