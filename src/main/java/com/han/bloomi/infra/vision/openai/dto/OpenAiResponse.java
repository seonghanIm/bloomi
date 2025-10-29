package com.han.bloomi.infra.vision.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record OpenAiResponse(
    String id,
    String object,
    Long created,
    String model,
    List<Choice> choices,
    Usage usage
) {
    @Builder
    public record Choice(
        Integer index,
        Message message,
        @JsonProperty("finish_reason") String finishReason
    ) {}

    @Builder
    public record Message(
        String role,
        String content
    ) {}

    @Builder
    public record Usage(
        @JsonProperty("prompt_tokens") Integer promptTokens,
        @JsonProperty("completion_tokens") Integer completionTokens,
        @JsonProperty("total_tokens") Integer totalTokens
    ) {}

    public String getContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        return choices.get(0).message().content();
    }
}