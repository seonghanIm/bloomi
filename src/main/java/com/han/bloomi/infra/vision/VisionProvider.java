package com.han.bloomi.infra.vision;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Vision API 프로바이더 종류
 */
@Getter
@RequiredArgsConstructor
public enum VisionProvider {
    OPENAI("OpenAI", "gpt-4o-mini"),
    ANTHROPIC("Anthropic", "claude-3-5-sonnet-20241022"),
    GOOGLE("Google", "gemini-pro-vision");

    private final String displayName;
    private final String defaultModel;
}