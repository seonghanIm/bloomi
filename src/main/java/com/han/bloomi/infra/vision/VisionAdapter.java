package com.han.bloomi.infra.vision;

import com.han.bloomi.domain.model.MealAnalysis;
import com.han.bloomi.domain.model.MealAnalysisRequest;
import com.han.bloomi.domain.port.VisionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * VisionPort의 구현체
 *
 * 도메인 포트와 인프라 어댑터를 연결하는 역할을 합니다.
 * 실제 Vision API 통신은 VisionClient 구현체에 위임합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisionAdapter implements VisionPort {
    private final VisionClient visionClient;
    private final PromptFactory promptFactory;

    @Override
    public MealAnalysis analyze(MealAnalysisRequest request) {
        log.info("Analyzing meal with provider: {}", visionClient.getProvider());

        // 프롬프트 생성
        String prompt = promptFactory.createPrompt(request);

        // Vision API 호출
        MealAnalysis analysis = visionClient.analyze(request, prompt);

        log.info("Meal analysis completed. Calories: {}, Confidence: {}",
                analysis.calories(), analysis.confidence());

        return analysis;
    }
}