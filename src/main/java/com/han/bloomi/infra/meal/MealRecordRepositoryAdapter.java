package com.han.bloomi.infra.meal;

import com.han.bloomi.common.error.ErrorCode;
import com.han.bloomi.common.exception.BusinessException;
import com.han.bloomi.domain.model.Macros;
import com.han.bloomi.domain.model.MealRecord;
import com.han.bloomi.domain.model.Serving;
import com.han.bloomi.domain.port.MealRecordRepository;
import com.han.bloomi.infra.user.UserEntity;
import com.han.bloomi.infra.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 식단 기록 저장소 어댑터
 */
@Repository
@RequiredArgsConstructor
public class MealRecordRepositoryAdapter implements MealRecordRepository {
    private final MealRecordJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public MealRecord save(MealRecord record) {
        // userId로 UserEntity 조회
        UserEntity user = userJpaRepository.findById(record.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MealRecordEntity entity = MealRecordEntity.builder()
                .id(record.id())
                .user(user)
                .imageUrl(record.imageUrl())
                .name(record.name())
                .calories(record.calories())
                .carbs(record.macros().carbs())
                .protein(record.macros().protein())
                .fat(record.macros().fat())
                .servingUnit(record.serving().unit())
                .servingAmount(record.serving().amount())
                .confidence(record.confidence())
                .advice(record.advice())
                .userInputName(record.userInputName())
                .userInputWeight(record.userInputWeight())
                .notes(record.notes())
                .analyzedAt(record.analyzedAt())
                .build();

        MealRecordEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MealRecord> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<MealRecord> findByUserId(String userId) {
        return jpaRepository.findByUser_Id(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<MealRecord> findByUserIdAndAnalyzedAt(String userId, LocalDate analyzedAt) {
        return jpaRepository.findByUser_IdAndAnalyzedAt(userId, analyzedAt).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<MealRecord> findByUserIdAndAnalyzedAtBetween(String userId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByUser_IdAndAnalyzedAtBetween(userId, startDate, endDate).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    private MealRecord toDomain(MealRecordEntity entity) {
        return MealRecord.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .imageUrl(entity.getImageUrl())
                .name(entity.getName())
                .calories(entity.getCalories())
                .macros(new Macros(entity.getCarbs(), entity.getProtein(), entity.getFat()))
                .serving(new Serving(entity.getServingUnit(), entity.getServingAmount()))
                .confidence(entity.getConfidence())
                .advice(entity.getAdvice())
                .userInputName(entity.getUserInputName())
                .userInputWeight(entity.getUserInputWeight())
                .notes(entity.getNotes())
                .analyzedAt(entity.getAnalyzedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}