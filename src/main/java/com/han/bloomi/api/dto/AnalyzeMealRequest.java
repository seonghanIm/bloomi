package com.han.bloomi.api.dto;

import com.han.bloomi.domain.model.MealEmotion;
import com.han.bloomi.domain.model.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "식단 분석 요청")
public class AnalyzeMealRequest {

    @Schema(description = "음식 이미지 파일", required = true)
    private MultipartFile image;

    @Schema(description = "음식 이름 (분석 힌트)", example = "닭가슴살 샐러드")
    private String name;

    @Schema(description = "중량(g) 또는 용량(ml)", example = "350")
    private Double weight;

    @Schema(description = "메모", example = "점심 식사")
    private String notes;

    @Schema(description = "식사 타입", example = "LUNCH")
    private MealType mealType;

    @Schema(description = "감정 상태", example = "HAPPY")
    private MealEmotion emotion;

    @Schema(description = "식사 장소 (자유 텍스트)", example = "화곡동 서연이네")
    private String location;

    @Schema(description = "식사 참여자 목록", example = "[\"나\", \"천지동\", \"김정윤\"]")
    private List<String> participants;
}
