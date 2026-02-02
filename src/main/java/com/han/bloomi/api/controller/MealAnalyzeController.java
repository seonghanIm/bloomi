package com.han.bloomi.api.controller;

import com.han.bloomi.api.dto.AnalyzeMealRequest;
import com.han.bloomi.api.dto.AnalyzeMealResponse;
import com.han.bloomi.api.dto.MealResponse;
import com.han.bloomi.api.dto.MonthlyMealStatisticsResponse;
import com.han.bloomi.api.dto.SaveMealRequest;
import com.han.bloomi.application.service.MealAnalyzeService;
import com.han.bloomi.common.response.CustomApiResponse;
import com.han.bloomi.common.swagger.ApiCommonResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Tag(name = "Meal Analysis", description = "식단 이미지 분석 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/meal")
@RequiredArgsConstructor
public class MealAnalyzeController {
    private final MealAnalyzeService service;

    @Operation(
        summary = "1단계: 식단 이미지 분석 (저장 X)",
        description = "업로드한 음식 이미지를 분석하여 칼로리와 영양 성분을 추정합니다. DB에 저장하지 않고 분석 결과만 반환합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = AnalyzeMealRequest.class)))
    @ApiResponse(responseCode = "200", description = "분석 성공", content = @Content(schema = @Schema(implementation = AnalyzeMealResponse.class)))
    @ApiCommonResponses.AuthenticatedFileUpload
    @ApiCommonResponses.ExternalApiError
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CustomApiResponse<AnalyzeMealResponse> analyze(@ModelAttribute AnalyzeMealRequest request) {
        log.info("Received analyze request - image: {}, name: {}, weight: {}",
                request.getImage().getOriginalFilename(),
                request.getName(),
                request.getWeight());

        AnalyzeMealResponse response = service.analyze(request);
        return CustomApiResponse.success("Meal analysis completed", response);
    }

    @Operation(
        summary = "2단계: 식단 저장",
        description = "분석 결과와 사용자 메타데이터(날짜, 감정, 장소 등)를 함께 저장합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "저장 성공", content = @Content(schema = @Schema(implementation = MealResponse.class)))
    @ApiCommonResponses.AuthRequired
    @PostMapping("/save")
    public CustomApiResponse<MealResponse> save(@Valid @org.springframework.web.bind.annotation.RequestBody SaveMealRequest request) {
        log.info("Received save request - name: {}, date: {}", request.name(), request.analyzedAt());

        MealResponse response = service.save(request);
        return CustomApiResponse.success("Meal saved successfully", response);
    }

    @Operation(
        summary = "식단 상세 조회",
        description = "특정 식단을 상세 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiCommonResponses.AuthRequired
    @GetMapping("/detail/{id}")
    public CustomApiResponse<MealResponse> detail(@PathVariable String id) {
        log.info("Received detail request - id: {}", id);
        MealResponse response = service.findMeal(id);
        return CustomApiResponse.success("Meal retrieved successfully", response);
    }

    @Operation(
        summary = "날짜별 식단 조회",
        description = "특정 날짜의 모든 식단 기록을 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiCommonResponses.AuthRequired
    @GetMapping("/{date}")
    public CustomApiResponse<List<MealResponse>> findDailyMeals(@PathVariable LocalDate date) {
        log.info("Received findDailyMeals request - date: {}", date);
        List<MealResponse> responses = service.findDailyMealsByDate(date);
        return CustomApiResponse.success("Meal records retrieved successfully", responses);
    }

    @Operation(
        summary = "월별 식단 통계 조회",
        description = "특정 월의 날짜별 식단 기록 건수를 조회합니다. yearMonth 형식: YYYY-MM (예: 2025-01)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MonthlyMealStatisticsResponse.class)))
    @ApiCommonResponses.AuthRequired
    @GetMapping("/monthly/{yearMonth}")
    public CustomApiResponse<MonthlyMealStatisticsResponse> getMonthlyStatistics(@PathVariable YearMonth yearMonth) {
        log.info("Received getMonthlyStatistics request - yearMonth: {}", yearMonth);
        MonthlyMealStatisticsResponse response = service.getMonthlyStatistics(yearMonth);
        return CustomApiResponse.success("Monthly statistics retrieved successfully", response);
    }
}