package com.han.bloomi.infra.user;

import com.han.bloomi.domain.model.user.User;
import com.han.bloomi.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA 기반 사용자 저장소 어댑터
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        UserEntity entity = jpaRepository.findById(user.id())
                .map(existing -> {
                    existing.update(user.name(), user.picture());
                    return existing;
                })
                .orElseGet(() -> UserEntity.builder()
                        .id(user.id())
                        .email(user.email())
                        .name(user.name())
                        .picture(user.picture())
                        .provider(user.provider())
                        .providerId(user.providerId())
                        .membership(user.membership())
                        .build());

        UserEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return jpaRepository.findByProviderAndProviderId(provider, providerId).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public User delete(String id) {
        UserEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // 도메인 객체로 변환 후 delete() 호출 (비즈니스 로직은 도메인에서)
        User user = toDomain(entity);
        User deletedUser = user.delete();

        // Entity에 반영
        entity.delete();
        jpaRepository.save(entity);

        return deletedUser;
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .picture(entity.getPicture())
                .provider(entity.getProvider())
                .providerId(entity.getProviderId())
                .membership(entity.getMembership())
                .deleted(entity.getDeleted())
                .deletedAt(entity.getDeletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}