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

    /**
     * 모든 사용자의 일일 요청 카운트를 0으로 리셋합니다.
     * 스케줄러에서 자정에 호출됩니다.
     */
    void resetAllDailyRequestCounts();

    /**
     * 약관 동의 정보를 저장합니다.
     * @param userId 사용자 ID
     * @param termsAgreed 서비스 이용 약관 동의
     * @param privacyAgreed 개인정보 수집 동의
     * @param marketingAgreed 마케팅 정보 수신 동의
     * @return 업데이트된 사용자
     */
    User agreeToTerms(String userId, Boolean termsAgreed, Boolean privacyAgreed, Boolean marketingAgreed);

    /**
     * 닉네임 중복 여부를 확인합니다.
     * @param nickname 닉네임
     * @return 이미 존재하면 true
     */
    boolean existsByNickname(String nickname);

    /**
     * 온보딩을 완료합니다.
     * @param userId 사용자 ID
     * @param nickname 닉네임
     * @param gender 성별
     * @param ageRange 연령대
     * @return 업데이트된 사용자
     */
    User completeOnboarding(String userId, String nickname, com.han.bloomi.domain.model.user.Gender gender, com.han.bloomi.domain.model.user.AgeRange ageRange);
}
