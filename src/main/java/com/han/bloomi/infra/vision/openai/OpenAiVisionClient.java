package com.han.bloomi.infra.vision.openai;

import com.han.bloomi.common.error.ErrorCode;
import com.han.bloomi.common.exception.BusinessException;
import com.han.bloomi.common.exception.VisionException;
import com.han.bloomi.domain.model.MealAnalysis;
import com.han.bloomi.domain.model.MealAnalysisRequest;
import com.han.bloomi.infra.vision.VisionClient;
import com.han.bloomi.infra.vision.VisionProvider;
import com.han.bloomi.infra.vision.openai.dto.OpenAiRequest;
import com.han.bloomi.infra.vision.openai.dto.OpenAiResponse;
import com.han.bloomi.infra.vision.openai.dto.VisionAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OpenAI Vision API 클라이언트 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bloomi.vision.provider", havingValue = "openai", matchIfMissing = true)
public class OpenAiVisionClient implements VisionClient {
    private final OpenAiProperties properties;
    private final ImageEncoder imageEncoder;
    private final OpenAiHttpClient httpClient;
    private final OpenAiResponseMapper responseMapper;

    @Override
    public VisionProvider getProvider() {
        return VisionProvider.OPENAI;
    }

    @Override
    public MealAnalysis analyze(MealAnalysisRequest request, String prompt) {
        log.info("OpenAI Vision API call - Image: {}, Name: {}, Weight: {}",
                request.image().getOriginalFilename(),
                request.name(),
                request.weight());

        // 1. 이미지 → base64 인코딩
        String base64Image = imageEncoder.encodeToBase64(request.image());
        log.debug("Image encoded to base64, length: {}", base64Image.length());

        // 2. OpenAI API 요청 생성
        OpenAiRequest apiRequest = buildRequest(prompt, base64Image);

        // 3. OpenAI Chat Completions API 호출 (vision 모델)
        OpenAiResponse apiResponse = httpClient.callChatCompletion(apiRequest);

        // 4. 응답 JSON 파싱
        String content = apiResponse.getContent();


        if (content == null || content.isBlank()|| "{}".equals(content)) {
            log.warn("Empty content from OpenAI API");
            throw new VisionException(ErrorCode.VISION_API_ERROR, "Empty content from OpenAI API");
        }

        VisionAnalysisResult analysisResult = httpClient.parseContent(content, VisionAnalysisResult.class);

        // 5. MealAnalysis 도메인 객체로 변환
        MealAnalysis result = responseMapper.toDomain(analysisResult);

        if(result.advice().equals("no meal") || result.advice().isBlank()) {
            log.warn("No meal");
            throw new VisionException(ErrorCode.VISION_NO_MEAL, "No meal");
        }

        log.info("OpenAI analysis completed - Calories: {}, Confidence: {}",
                result.calories(), result.confidence());

        return result;
    }

    @Override
    public boolean isAvailable() {
        return properties.getApiKey() != null && !properties.getApiKey().isBlank();
    }

    private OpenAiRequest buildRequest(String prompt, String base64Image) {
        List<OpenAiRequest.Content> contents = List.of(
                OpenAiRequest.Content.text(prompt),
                OpenAiRequest.Content.image(base64Image)
        );

        OpenAiRequest.Message message = OpenAiRequest.Message.user(contents);

        return OpenAiRequest.builder()
                .model(properties.getModel())
                .messages(List.of(message))
                .maxTokens(properties.getMaxTokens())
                .temperature(properties.getTemperature())
                .responseFormat(OpenAiRequest.ResponseFormat.json())
                .build();
    }
}