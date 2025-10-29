package com.han.bloomi.infra.vision.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.han.bloomi.common.error.ErrorCode;
import com.han.bloomi.common.exception.VisionException;
import com.han.bloomi.infra.vision.openai.dto.OpenAiRequest;
import com.han.bloomi.infra.vision.openai.dto.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * OpenAI API HTTP 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiHttpClient {
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * OpenAI Chat Completions API 호출
     */
    public OpenAiResponse callChatCompletion(OpenAiRequest request) {
        String endpoint = properties.getBaseUrl() + "/chat/completions";

        try {
            log.info("Calling OpenAI API: model={}, messages={}",
                    request.model(), request.messages().size());

            RestClient restClient = RestClient.builder()
                    .baseUrl(properties.getBaseUrl())
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            OpenAiResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(OpenAiResponse.class);

            if (response == null) {
                throw new VisionException(ErrorCode.VISION_INVALID_RESPONSE, "Empty response from OpenAI");
            }

            log.info("OpenAI API call successful: id={}, tokens={}",
                    response.id(),
                    response.usage() != null ? response.usage().totalTokens() : 0);

            return response;

        } catch (HttpClientErrorException e) {
            log.error("OpenAI API client error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new VisionException(ErrorCode.VISION_API_ERROR,
                    "OpenAI API error: " + e.getStatusCode());

        } catch (HttpServerErrorException e) {
            log.error("OpenAI API server error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new VisionException(ErrorCode.VISION_API_ERROR,
                    "OpenAI server error: " + e.getStatusCode());

        } catch (ResourceAccessException e) {
            log.error("OpenAI API timeout or connection error", e);
            throw new VisionException(ErrorCode.VISION_TIMEOUT,
                    "OpenAI API timeout after " + properties.getTimeoutMs() + "ms");

        } catch (Exception e) {
            log.error("Unexpected error calling OpenAI API", e);
            throw new VisionException(ErrorCode.VISION_API_ERROR,
                    "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * OpenAI 응답의 content를 특정 타입으로 파싱
     */
    public <T> T parseContent(String content, Class<T> clazz) {
        try {
            return objectMapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse OpenAI response content", e);
            throw new VisionException(ErrorCode.VISION_INVALID_RESPONSE,
                    "Failed to parse JSON response from OpenAI");
        }
    }
}