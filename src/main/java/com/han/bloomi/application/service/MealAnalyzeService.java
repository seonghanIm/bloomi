package com.han.bloomi.application.service;

import com.han.bloomi.api.dto.AnalyzeMealRequest;
import com.han.bloomi.api.dto.AnalyzeMealResponse;
import com.han.bloomi.api.dto.MonthlyMealStatisticsResponse;
import com.han.bloomi.common.error.ErrorCode;
import com.han.bloomi.common.exception.BusinessException;
import com.han.bloomi.common.trace.TraceIdHolder;
import com.han.bloomi.domain.model.FoodItem;
import com.han.bloomi.domain.model.MealAnalysis;
import com.han.bloomi.domain.model.MealAnalysisRequest;
import com.han.bloomi.domain.model.MealRecord;
import com.han.bloomi.domain.model.user.User;
import com.han.bloomi.domain.port.MealRecordRepository;
import com.han.bloomi.domain.port.UserRepository;
import com.han.bloomi.domain.port.VisionPort;
import com.han.bloomi.infra.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public AnalyzeMealResponse analyze(AnalyzeMealRequest request) {
        String traceId = traceIdHolder.current();
        String userId = currentUserService.getCurrentUserId();
        log.info("[{}] Starting meal analysis - userId: {}, image: {}", traceId, userId, request.getImage().getOriginalFilename());

        // 0. 일일 요청 제한 체크
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.hasExceededDailyLimit()) {
            log.warn("[{}] Daily limit exceeded - userId: {}, membership: {}, dailyRequestCount: {}",
                    traceId, userId, user.membership(), user.dailyRequestCount());
            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED,
                    String.format("User %s has exceeded daily limit (count: %d)", userId, user.dailyRequestCount()));
        }

        // 1. S3에 이미지 업로드
        String imageUrl = imageUploadService.uploadMealImage(request.getImage(), userId);
        log.info("[{}] Image uploaded to S3: {}", traceId, imageUrl);

        // 2. Vision API로 분석
        MealAnalysisRequest domainRequest = MealAnalysisRequest.of(
                request.getImage(),
                request.getName(),
                request.getWeight(),
                request.getNotes()
        );
        MealAnalysis analysis = visionPort.analyze(domainRequest);
        log.info("[{}] Vision analysis completed - calories: {}", traceId, analysis.calories());

        // 3. DB에 분석 결과 저장 (도메인 로직)
        String recordId = UUID.randomUUID().toString();
        MealRecord mealRecord = MealRecord.of(
                recordId,
                userId,
                imageUrl,
                analysis,
                request.getName(),
                request.getWeight(),
                request.getNotes()
        );
        mealRecordRepository.save(mealRecord);
        log.info("[{}] Meal record saved to DB - recordId: {}", traceId, recordId);

        // 4. 요청 카운트 증가
        userRepository.incrementDailyRequestCount(userId);
        log.info("[{}] Daily request count incremented for user: {}", traceId, userId);

        // 5. API 응답 생성
        AnalyzeMealResponse response = toResponse(analysis, traceId);
        log.info("[{}] Meal analysis completed successfully", traceId);
        return response;
    }

    // 일별 조회
    public List<AnalyzeMealResponse> findDailyMeals(LocalDate date) {
        String traceId = traceIdHolder.current();
        String userId = currentUserService.getCurrentUserId();
        log.info("[{}] Querying meal records - userId: {}, date: {}", traceId, userId, date);

        List<MealRecord> records = mealRecordRepository.findByUserIdAndAnalyzedAt(userId, date);
        log.info("[{}] Found {} meal records for date: {}", traceId, records.size(), date);

        return records.stream()
                .map(record -> toResponseFromRecord(record, traceId))
                .toList();
    }

    // 월별 통계 조회
    public MonthlyMealStatisticsResponse getMonthlyStatistics(YearMonth yearMonth) {
        String traceId = traceIdHolder.current();
        String userId = currentUserService.getCurrentUserId();
        log.info("[{}] Querying monthly statistics - userId: {}, yearMonth: {}", traceId, userId, yearMonth);

        // 해당 월의 시작일과 마지막일 계산
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 해당 월의 모든 식단 기록 조회
        List<MealRecord> records = mealRecordRepository.findByUserIdAndAnalyzedAtBetween(userId, startDate, endDate);
        log.info("[{}] Found {} meal records for month: {}", traceId, records.size(), yearMonth);

        // 날짜별로 그룹핑하여 건수 집계
        Map<LocalDate, Long> dailyCounts = records.stream()
                .collect(Collectors.groupingBy(
                        MealRecord::analyzedAt,
                        Collectors.counting()
                ));

        return MonthlyMealStatisticsResponse.builder()
                .yearMonth(yearMonth.toString())
                .dailyCounts(dailyCounts)
                .totalCount(records.size())
                .traceId(traceId)
                .build();
    }

    private AnalyzeMealResponse toResponse(MealAnalysis analysis, String traceId) {
        return AnalyzeMealResponse.builder()
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

    private AnalyzeMealResponse toResponseFromRecord(MealRecord record, String traceId) {
        return AnalyzeMealResponse.builder()
                .name(record.name())
                .calories(record.calories())
                .macros(AnalyzeMealResponse.Macros.builder()
                        .carbs(record.macros().carbs())
                        .protein(record.macros().protein())
                        .fat(record.macros().fat())
                        .build())
                .serving(AnalyzeMealResponse.Serving.builder()
                        .unit(record.serving().unit())
                        .amount(record.serving().amount())
                        .build())
                .items(List.of()) // 저장된 레코드에는 개별 음식 항목 정보가 없음
                .confidence(record.confidence())
                .advice(record.advice())
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