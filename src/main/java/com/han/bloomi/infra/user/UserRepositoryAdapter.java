package com.han.bloomi.infra.user;

import com.han.bloomi.domain.model.User;
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

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .picture(entity.getPicture())
                .provider(entity.getProvider())
                .providerId(entity.getProviderId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}