package com.han.bloomi.infra.vision.openai;

import com.han.bloomi.domain.model.FoodItem;
import com.han.bloomi.domain.model.Macros;
import com.han.bloomi.domain.model.MealAnalysis;
import com.han.bloomi.domain.model.Serving;
import com.han.bloomi.infra.vision.openai.dto.VisionAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * OpenAI API 응답을 도메인 모델로 변환하는 매퍼
 */
@Component
public class OpenAiResponseMapper {

    public MealAnalysis toDomain(VisionAnalysisResult result) {
        return MealAnalysis.builder()
                .name(result.name() != null ? result.name() : "음식")
                .calories(result.calories() != null ? result.calories() : 0.0)
                .macros(mapMacros(result.macros()))
                .serving(mapServing(result.serving()))
                .items(mapItems(result.items()))
                .confidence(result.confidence() != null ? result.confidence() : 0.0)
                .advice(result.advice() != null ? result.advice() : "")
                .build();
    }

    private Macros mapMacros(VisionAnalysisResult.Macros macros) {
        if (macros == null) {
            return Macros.of(0.0, 0.0, 0.0);
        }
        return Macros.of(
                macros.carbs() != null ? macros.carbs() : 0.0,
                macros.protein() != null ? macros.protein() : 0.0,
                macros.fat() != null ? macros.fat() : 0.0
        );
    }

    private Serving mapServing(VisionAnalysisResult.Serving serving) {
        if (serving == null) {
            return Serving.of("g", 0.0);
        }
        return Serving.of(
                serving.unit() != null ? serving.unit() : "g",
                serving.amount() != null ? serving.amount() : 0.0
        );
    }

    private List<FoodItem> mapItems(List<VisionAnalysisResult.Item> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(item -> FoodItem.of(
                        item.name() != null ? item.name() : "Unknown",
                        item.amount() != null ? item.amount() : 0.0,
                        item.unit() != null ? item.unit() : "g",
                        item.calories() != null ? item.calories() : 0.0
                ))
                .toList();
    }
}