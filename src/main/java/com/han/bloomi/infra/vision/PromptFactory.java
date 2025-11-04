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
            - 음식 사진을 분석하여 음식 양 기준 칼로리와 3대 영양소(탄수화물, 단백질, 지방)를 추정합니다.
            - 음식명(name)을 간단히 요약해서 제공합니다. 사용자가 음식명을 제공하면 그것을 사용하고, 없으면 AI가 판단한 음식명을 제공합니다.
            - 확신도(confidence)를 0~1 사이의 값으로 제공합니다.
            - 개별 음식 항목(items)별로 상세 정보를 제공합니다.
            - 영양학적 조언(advice)을 한 문장으로 제공합니다.
            - 음식 양을 제공하지 않는 경우에는 1인분 기준으로 측정합니다.
            - 브랜드 음식인 경우 해당 브랜드에서 제공하는 정확한 칼로리를 가지고 중량과 함께 계산합니다.
            - 식품 데이터 베이스를 참고해서 정확도를 높입니다.
            - 음식이 아닌경우 json 을 만들지 않고 "no meal" 을 제공합니다.
            
            응답 형식 (JSON만 출력):
            {
              "name": string,
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

            음식명(name) 규칙:
            - 사용자가 음식명을 제공한 경우: 사용자가 제공한 음식명을 그대로 사용
            - 사용자가 음식명을 제공하지 않은 경우: AI가 분석한 주요 음식들을 쉼표로 연결 (예: "닭가슴살, 현미밥, 샐러드")
            - 최대 3.
            개 항목까지만 포함
            """;

    public String createPrompt(MealAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder(BASE_PROMPT);

        prompt.append("\n\n추가 정보:\n");

        if (request.hasName()) {
            prompt.append("- 사용자 입력 음식명: ").append(request.name()).append("\n");
            prompt.append("  → name 필드에는 이 값을 그대로 사용하세요.\n");
        } else {
            prompt.append("- 사용자가 음식명을 입력하지 않았습니다.\n");
            prompt.append("  → name 필드에 AI가 분석한 주요 음식명을 작성하세요 (최대 3개, 쉼표로 구분).\n");
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

        prompt.append("\n음식 사진이 아닌 경우 빈 값을 응답하세요.\n");

        return prompt.toString();
    }
}