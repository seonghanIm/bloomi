package com.han.bloomi.infra.vision;

import com.han.bloomi.domain.model.MealAnalysisRequest;
import org.springframework.stereotype.Component;

/**
 * Vision API에 전달할 프롬프트를 생성하는 팩토리
 */
@Component
public class PromptFactory {
    private static final String BASE_PROMPT = """
            당신은 영양 성분 분석 전문가입니다.

            목표:
            - 음식 사진을 분석하여 1회 섭취 기준 칼로리와 3대 영양소(탄수화물, 단백질, 지방)를 추정합니다.
            - 확신도(confidence)를 0~1 사이의 값으로 제공합니다.
            - 개별 음식 항목(items)별로 상세 정보를 제공합니다.
            - 영양학적 조언(advice)을 한 문장으로 제공합니다.

            응답 형식 (JSON만 출력):
            {
              "calories": number,
              "macros": {
                "carbs": number,
                "protein": number,
                "fat": number
              },
              "serving": {
                "unit": "g" | "ml",
                "amount": number
              },
              "items": [
                {
                  "name": string,
                  "amount": number,
                  "unit": "g" | "ml",
                  "calories": number
                }
              ],
              "confidence": number,
              "advice": string
            }
            """;

    public String createPrompt(MealAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder(BASE_PROMPT);

        prompt.append("\n\n추가 정보:\n");

        if (request.hasName()) {
            prompt.append("- 음식명: ").append(request.name()).append("\n");
        }

        if (request.hasWeight()) {
            prompt.append("- 중량/용량: ").append(request.weight()).append("g\n");
        }

        if (request.notes() != null && !request.notes().isBlank()) {
            prompt.append("- 기타: ").append(request.notes()).append("\n");
        }

        if (request.hasWeight()) {
            prompt.append("\n제공된 중량 정보를 우선적으로 고려하여 분석하세요.");
        } else {
            prompt.append("\n일반적인 1인분 기준으로 serving을 추정하세요.");
        }

        return prompt.toString();
    }
}