package com.han.bloomi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

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
}