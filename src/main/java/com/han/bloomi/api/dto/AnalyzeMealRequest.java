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
@Schema(description = "식단 분석 요청 (이미지 분석만, 저장 X)")
public class AnalyzeMealRequest {

    @Schema(description = "음식 이미지 파일", required = true)
    private MultipartFile image;

    @Schema(description = "음식 이름 힌트 (선택)", example = "닭가슴살 샐러드")
    private String name;

    @Schema(description = "중량 힌트 (g, 선택)", example = "350")
    private Double weight;
}