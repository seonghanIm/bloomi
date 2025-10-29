package com.han.bloomi.infra.vision.openai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bloomi.vision.openai")
public class OpenAiProperties {
    /**
     * OpenAI API 키
     */
    private String apiKey;

    /**
     * OpenAI API Base URL (기본값: https://api.openai.com/v1)
     */
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * 사용할 모델 (기본값: gpt-4o)
     */
    private String model = "gpt-4o-mini";

    /**
     * 최대 토큰 수
     */
    private Integer maxTokens = 2000;

    /**
     * Temperature (0~2, 기본값: 0.7)
     */
    private Double temperature = 0.7;

    /**
     * HTTP 타임아웃 (밀리초)
     */
    private Integer timeoutMs = 30000;
}