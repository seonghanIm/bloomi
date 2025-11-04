package com.han.bloomi.infra.user;

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
}