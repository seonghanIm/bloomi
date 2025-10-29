package com.han.bloomi.infra.vision.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record OpenAiRequest(
    String model,
    List<Message> messages,
    @JsonProperty("max_tokens") Integer maxTokens,
    Double temperature,
    @JsonProperty("response_format") ResponseFormat responseFormat
) {
    @Builder
    public record Message(
        String role,
        List<Content> content
    ) {
        public static Message user(List<Content> content) {
            return Message.builder()
                    .role("user")
                    .content(content)
                    .build();
        }
    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Content(
        String type,
        String text,
        @JsonProperty("image_url") ImageUrl imageUrl
    ) {
        public static Content text(String text) {
            return Content.builder()
                    .type("text")
                    .text(text)
                    .build();
        }

        public static Content image(String base64Data) {
            return Content.builder()
                    .type("image_url")
                    .imageUrl(ImageUrl.of(base64Data))
                    .build();
        }
    }

    @Builder
    public record ImageUrl(String url) {
        public static ImageUrl of(String base64Data) {
            return ImageUrl.builder()
                    .url("data:image/jpeg;base64," + base64Data)
                    .build();
        }
    }

    @Builder
    public record ResponseFormat(String type) {
        public static ResponseFormat json() {
            return ResponseFormat.builder()
                    .type("json_object")
                    .build();
        }
    }
}