package com.han.bloomi.domain.port;

import com.han.bloomi.domain.model.MealAnalysis;
import com.han.bloomi.domain.model.MealAnalysisRequest;

/**
 * Vision API와의 통신을 추상화한 도메인 포트
 *
 * 이 인터페이스는 도메인 레이어에 속하며, 구체적인 Vision 프로바이더(OpenAI, Anthropic 등)에
 * 의존하지 않습니다. 인프라 레이어에서 이 인터페이스를 구현하여 실제 API와 통신합니다.
 *
 * Port & Adapter 패턴을 적용하여 Vision 프로바이더를 쉽게 교체할 수 있습니다.
 */
public interface VisionPort {
    /**
     * 이미지와 힌트 정보를 기반으로 식사를 분석합니다.
     *
     * @param request 이미지, 음식명, 중량 등의 분석 요청 정보
     * @return Vision API로부터 분석된 식사 정보
     * @throws com.han.bloomi.common.exception.VisionException Vision API 호출 중 오류 발생 시
     */
    MealAnalysis analyze(MealAnalysisRequest request);
}