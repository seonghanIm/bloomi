package com.han.bloomi.application.service;

import com.han.bloomi.api.dto.AnalyzeMealRequest;
import com.han.bloomi.api.dto.AnalyzeMealResponse;
import com.han.bloomi.common.trace.TraceIdHolder;
import com.han.bloomi.domain.model.FoodItem;
import com.han.bloomi.domain.model.MealAnalysis;
import com.han.bloomi.domain.model.MealAnalysisRequest;
import com.han.bloomi.domain.port.VisionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealAnalyzeService {
    private final VisionPort visionPort;
    private final TraceIdHolder traceIdHolder;

    public AnalyzeMealResponse analyze(MultipartFile image, AnalyzeMealRequest request) {
        String traceId = traceIdHolder.current();
        log.info("[{}] Starting meal analysis - image: {}", traceId, image.getOriginalFilename());

        // API DTO → 도메인 모델 변환
        MealAnalysisRequest domainRequest = MealAnalysisRequest.of(
                image,
                request != null ? request.name() : null,
                request != null ? request.weight() : null,
                request != null ? request.notes() : null
        );

        // Vision API 호출
        MealAnalysis analysis = visionPort.analyze(domainRequest);

        // 도메인 모델 → API DTO 변환
        AnalyzeMealResponse response = toResponse(analysis, traceId);

        log.info("[{}] Meal analysis completed - calories: {}", traceId, response.calories());
        return response;
    }

    private AnalyzeMealResponse toResponse(MealAnalysis analysis, String traceId) {
        return AnalyzeMealResponse.builder()
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