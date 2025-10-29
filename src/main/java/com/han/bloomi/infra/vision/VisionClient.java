package com.han.bloomi.infra.vision;

import com.han.bloomi.domain.model.MealAnalysis;
import com.han.bloomi.domain.model.MealAnalysisRequest;

/**
 * Vision API 클라이언트 어댑터의 공통 인터페이스
 *
 * 각 Vision 프로바이더(OpenAI, Anthropic, Google 등)는 이 인터페이스를 구현합니다.
 * VisionPort를 구현하는 VisionAdapter가 이 인터페이스의 구현체들을 사용합니다.
 */
public interface VisionClient {
    /**
     * 이 클라이언트가 지원하는 프로바이더 정보를 반환합니다.
     *
     * @return Vision 프로바이더
     */
    VisionProvider getProvider();

    /**
     * Vision API를 호출하여 식사를 분석합니다.
     *
     * @param request 분석 요청 정보
     * @param prompt Vision API에 전달할 프롬프트
     * @return 분석 결과
     */
    MealAnalysis analyze(MealAnalysisRequest request, String prompt);

    /**
     * API 연결 상태를 확인합니다.
     *
     * @return 연결 가능 여부
     */
    boolean isAvailable();
}