package com.han.bloomi.infra.user;

import com.han.bloomi.domain.model.user.AgeRange;
import com.han.bloomi.domain.model.user.Gender;
import com.han.bloomi.domain.model.user.Membership;
import com.han.bloomi.infra.meal.MealRecordEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String picture;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Membership membership;

    @Column(nullable = false)
    private Integer dailyRequestCount = 0;

    private LocalDateTime lastRequestDate;

    @Column(nullable = false)
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    // 약관 동의 필드
    @Column(nullable = false)
    private Boolean termsAgreed = false;          // 서비스 이용 약관 동의

    @Column(nullable = false)
    private Boolean privacyAgreed = false;        // 개인정보 수집 동의

    @Column(nullable = false)
    private Boolean marketingAgreed = false;      // 마케팅 정보 수신 동의 (선택)

    private LocalDateTime termsAgreedAt;          // 약관 동의 시점

    // 온보딩 필드
    @Column(unique = true, length = 12)
    private String nickname;                      // 닉네임 (2~12자)

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;                        // 성별

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AgeRange ageRange;                    // 연령대

    @Column(nullable = false)
    private Boolean onboardingCompleted = false;  // 온보딩 완료 여부

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealRecordEntity> mealRecords = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserEntity(String id, String email, String name, String picture,
                      String provider, String providerId, Membership membership) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.provider = provider;
        this.providerId = providerId;
        this.membership = membership;
    }

    public void update(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void incrementDailyRequestCount() {
        LocalDateTime now = LocalDateTime.now();
        boolean isNewDay = this.lastRequestDate == null ||
                          !this.lastRequestDate.toLocalDate().equals(now.toLocalDate());

        if (isNewDay) {
            this.dailyRequestCount = 1;
        } else {
            this.dailyRequestCount++;
        }
        this.lastRequestDate = now;
    }

    public boolean hasExceededDailyLimit() {
        if (this.membership == Membership.FREE) {
            LocalDateTime now = LocalDateTime.now();
            boolean isSameDay = this.lastRequestDate != null &&
                              this.lastRequestDate.toLocalDate().equals(now.toLocalDate());
            return isSameDay && this.dailyRequestCount >= 3;
        }
        return false; // PREMIUM은 제한 없음
    }

    /**
     * 약관 동의 정보를 업데이트합니다.
     */
    public void agreeToTerms(Boolean termsAgreed, Boolean privacyAgreed, Boolean marketingAgreed) {
        this.termsAgreed = termsAgreed;
        this.privacyAgreed = privacyAgreed;
        this.marketingAgreed = marketingAgreed;
        this.termsAgreedAt = LocalDateTime.now();
    }

    /**
     * 필수 약관에 동의했는지 확인합니다.
     */
    public boolean hasAgreedToRequiredTerms() {
        return Boolean.TRUE.equals(this.termsAgreed) && Boolean.TRUE.equals(this.privacyAgreed);
    }

    /**
     * 온보딩 정보를 업데이트합니다.
     */
    public void completeOnboarding(String nickname, Gender gender, AgeRange ageRange) {
        this.nickname = nickname;
        this.gender = gender;
        this.ageRange = ageRange;
        this.onboardingCompleted = true;
    }

    /**
     * 온보딩이 완료되었는지 확인합니다.
     */
    public boolean hasCompletedOnboarding() {
        return Boolean.TRUE.equals(this.onboardingCompleted);
    }
}