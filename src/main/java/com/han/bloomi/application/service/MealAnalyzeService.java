package com.han.bloomi.application.service;

import com.han.bloomi.api.dto.AnalyzeMealRequest;
import com.han.bloomi.api.dto.AnalyzeMealResponse;
import com.han.bloomi.api.dto.MealResponse;
import com.han.bloomi.api.dto.MonthlyMealStatisticsResponse;
import com.han.bloomi.api.dto.SaveMealRequest;
import com.han.bloomi.common.error.ErrorCode;
import com.han.bloomi.common.exception.BusinessException;
import com.han.bloomi.common.trace.TraceIdHolder;
import com.han.bloomi.domain.model.FoodItem;
import com.han.bloomi.domain.model.Macros;
import com.han.bloomi.domain.model.MealAnalysis;
import com.han.bloomi.domain.model.MealAnalysisRequest;
import com.han.bloomi.domain.model.MealRecord;
import com.han.bloomi.domain.model.Serving;
import com.han.bloomi.domain.model.user.User;
import com.han.bloomi.domain.port.MealRecordRepository;
import com.han.bloomi.domain.port.UserRepository;
import com.han.bloomi.domain.port.VisionPort;
import com.han.bloomi.infra.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealAnalyzeService {
    private final VisionPort visionPort;
    private final MealRecordRepository mealRecordRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final TraceIdHolder traceIdHolder;
    private final ImageUploadService imageUploadService;
    private final ImageProcessor imageProcessor;

    /**
     * 1단계: 이미지 분석만 수행 (DB 저장 X)
     * - 이미지를 S3에 업로드
     * - Vision API로 분석
     * - 분석 결과만 반환
     */
    @Transactional
    public AnalyzeMealResponse analyze(AnalyzeMealRequest request) {
        String traceId = traceIdHolder.current();
        String userId = currentUserService.getCurrentUserId();
        log.info("[{}] Starting meal analysis (analyze only) - userId: {}", traceId, userId);

        // 0. 일일 요청 제한 체크
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.hasExceededDailyLimit()) {
            log.warn("[{}] Daily limit exceeded - userId: {}", traceId, userId);
            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        // 1. 이미지 최적화
        MultipartFile optimizedImage;
        try {
            optimizedImage = imageProcessor.optimize(request.getImage());
            log.info("[{}] Image optimized", traceId);
        } catch (Exception e) {
            log.error("[{}] Image optimization failed, using original", traceId, e);
            optimizedImage = request.getImage();
        }

        // 2. S3에 이미지 업로드
        String imageUrl = imageUploadService.uploadMealImage(optimizedImage, userId);
        log.info("[{}] Image uploaded to S3: {}", traceId, imageUrl);

        // 3. Vision API로 분석
        MealAnalysisRequest domainRequest = MealAnalysisRequest.of(
                optimizedImage,
                request.getName(),
                request.getWeight(),
                null  // notes는 저장 단계에서 입력
        );
        MealAnalysis analysis = visionPort.analyze(domainRequest);
        log.info("[{}] Vision analysis completed - calories: {}", traceId, analysis.calories());

        // 4. 요청 카운트 증가
        userRepository.incrementDailyRequestCount(userId);
        log.info("[{}] Daily request count incremented", traceId);

        // 5. 분석 결과만 반환 (DB 저장 X)
        return toAnalyzeResponse(analysis, imageUrl, traceId);
    }

    /**
     * 2단계: 분석 결과 + 사용자 메타데이터 저장
     */
    @Transactional
    public MealResponse save(SaveMealRequest request) {
        String traceId = traceIdHolder.current();
        String userId = currentUserService.getCurrentUserId();
        log.info("[{}] Saving meal record - userId: {}, name: {}", traceId, userId, request.name());

        // 1. MealRecord 생성
        String recordId = UUID.randomUUID().toString();
        MealRecord mealRecord = MealRecord.builder()
                .id(recordId)
                .userId(userId)
                .imageUrl(request.imageUrl())
                .name(request.name())
                .calories(request.calories())
                .macros(new Macros(request.carbs(), request.protein(), request.fat()))
                .serving(new Serving(request.servingUnit(), request.servingAmount()))
                .confidence(request.confidence() != null ? request.confidence() : 0.0)
                .advice(request.advice())
                .userInputName(null)
                .userInputWeight(null)
                .notes(request.notes())
                .mealType(request.mealType())
                .emotion(request.emotion())
                .location(request.location())
                .participants(request.participants())
                .analyzedAt(request.analyzedAt())
                .createdAt(java.time.LocalDateTime.now())
                .build();

        // 2. DB 저장
        MealRecord saved = mealRecordRepository.save(mealRecord);
        log.info("[{}] Meal record saved - id: {}", traceId, saved.id());

        return toMealResponse(saved, traceId);
    }

    /**
     * 식단 상세 조회
     */
    public MealResponse findMeal(String id) {
        String traceId = traceIdHolder.current();
        MealRecord record = mealRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_MEAL_ID));
        return toMealResponse(record, traceId);
    }

    /**
     * 일별 조회
     */
    public List<MealResponse> findDailyMealsByDate(LocalDate date) {
        String traceId = traceIdHolder.current();
        String userId = currentUserService.getCurrentUserId();
        log.info("[{}] Querying meal records - userId: {}, date: {}", traceId, userId, date);

        List<MealRecord> records = mealRecordRepository.findByUserIdAndAnalyzedAt(userId, date);
        log.info("[{}] Found {} meal records for date: {}", traceId, records.size(), date);

        return records.stream()
                .map(record -> toMealResponse(record, traceId))
                .toList();
    }

    /**
     * 월별 통계 조회
     */
    public MonthlyMealStatisticsResponse getMonthlyStatistics(YearMonth yearMonth) {
        String traceId = traceIdHolder.current();
        String userId = currentUserService.getCurrentUserId();
        log.info("[{}] Querying monthly statistics - yearMonth: {}", traceId, yearMonth);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<MealRecord> records = mealRecordRepository.findByUserIdAndAnalyzedAtBetween(userId, startDate, endDate);

        Map<LocalDate, Long> dailyCounts = records.stream()
                .collect(Collectors.groupingBy(MealRecord::analyzedAt, Collectors.counting()));

        return MonthlyMealStatisticsResponse.builder()
                .yearMonth(yearMonth.toString())
                .dailyCounts(dailyCounts)
                .totalCount(records.size())
                .traceId(traceId)
                .build();
    }

    // === Private Helper Methods ===

    private AnalyzeMealResponse toAnalyzeResponse(MealAnalysis analysis, String imageUrl, String traceId) {
        return AnalyzeMealResponse.builder()
                .imageUrl(imageUrl)
                .name(analysis.name())
                .calories(analysis.calories())
                .macros(AnalyzeMealResponse.Macros.builder()
                        .carbs(analysis.macros().carbs())
                        .protein(analysis.macros().protein())
                        .fat(analysis.macros().fat())
                        .build())
                .serving(AnalyzeMealResponse.Serving.builder()
                        .unit(analysis.serving().unit())
                        .amount(analysis.serving().amount())
                        .build())
                .items(mapItems(analysis.items()))
                .confidence(analysis.confidence())
                .advice(analysis.advice())
                .traceId(traceId)
                .build();
    }

    private MealResponse toMealResponse(MealRecord record, String traceId) {
        return MealResponse.builder()
                .id(record.id())
                .imageUrl(record.imageUrl())
                .name(record.name())
                .calories(record.calories())
                .macros(MealResponse.Macros.builder()
                        .carbs(record.macros().carbs())
                        .protein(record.macros().protein())
                        .fat(record.macros().fat())
                        .build())
                .serving(MealResponse.Serving.builder()
                        .unit(record.serving().unit())
                        .amount(record.serving().amount())
                        .build())
                .confidence(record.confidence())
                .advice(record.advice())
                .analyzedAt(record.analyzedAt())
                .mealType(record.mealType())
                .emotion(record.emotion())
                .notes(record.notes())
                .participants(record.participants())
                .location(record.location())
                .traceId(traceId)
                .build();
    }

    private List<AnalyzeMealResponse.Item> mapItems(List<FoodItem> items) {
        return items.stream()
                .map(item -> AnalyzeMealResponse.Item.builder()
                        .name(item.name())
                        .amount(item.amount())
                        .unit(item.unit())
                        .calories(item.calories())
                        .build())
                .toList();
    }
}