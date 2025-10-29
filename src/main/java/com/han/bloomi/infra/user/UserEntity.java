package com.han.bloomi.infra.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserEntity(String id, String email, String name, String picture,
                      String provider, String providerId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void update(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }
}