package com.han.bloomi.api.controller;

import com.han.bloomi.api.dto.AnalyzeMealRequest;
import com.han.bloomi.api.dto.AnalyzeMealResponse;
import com.han.bloomi.api.dto.MonthlyMealStatisticsResponse;
import com.han.bloomi.application.service.MealAnalyzeService;
import com.han.bloomi.common.response.ApiResponse;
import com.han.bloomi.common.swagger.ApiCommonResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            summary = "식단 이미지 분석",
            description = "업로드한 음식 이미지를 분석하여 칼로리와 영양 성분을 추정합니다. " +
                    "이미지는 필수이며, 음식명/중량/메모는 선택사항입니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = AnalyzeMealRequest.class)
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "분석 성공",
            content = @Content(schema = @Schema(implementation = AnalyzeMealResponse.class))
    )
    @ApiCommonResponses.AuthenticatedFileUpload
    @ApiCommonResponses.ExternalApiError
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AnalyzeMealResponse> analyze(@ModelAttribute AnalyzeMealRequest request) {
        log.info("Received analyze request - image: {}, name: {}, weight: {}",
                request.getImage().getOriginalFilename(),
                request.getName(),
                request.getWeight());

        AnalyzeMealResponse response = service.analyze(request);
        return ApiResponse.success("Meal analysis completed", response);
    }


    @Operation(
            summary = "날짜별 식단 조회",
            description = "특정 날짜의 모든 식단 기록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
    )
    @ApiCommonResponses.AuthRequired
    @GetMapping("/{date}")
    public ApiResponse<List<AnalyzeMealResponse>> findDailyMeals(@PathVariable LocalDate date) {
        log.info("Received findDailyMeals request - date: {}", date);
        List<AnalyzeMealResponse> responses = service.findDailyMeals(date);
        return ApiResponse.success("Meal records retrieved successfully", responses);
    }

    @Operation(
            summary = "월별 식단 통계 조회",
            description = "특정 월의 날짜별 식단 기록 건수를 조회합니다. " +
                    "yearMonth 형식: YYYY-MM (예: 2025-01)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = MonthlyMealStatisticsResponse.class))
    )
    @ApiCommonResponses.AuthRequired
    @GetMapping("/monthly/{yearMonth}")
    public ApiResponse<MonthlyMealStatisticsResponse> getMonthlyStatistics(@PathVariable YearMonth yearMonth) {
        log.info("Received getMonthlyStatistics request - yearMonth: {}", yearMonth);
        MonthlyMealStatisticsResponse response = service.getMonthlyStatistics(yearMonth);
        return ApiResponse.success("Monthly statistics retrieved successfully", response);
    }
}