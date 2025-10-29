# BLOOMI — Backend Brief (claude.md)

> 목적: BLOOMI 1차 목표(사진 → 칼로리 추정 API) 백엔드 개발 가이드를 일관된 톤으로 정리. Claude/ChatGPT 등 LLM에게 전달 가능한 요약 프롬프트와 아키텍처, 스펙, 샘플 코드를 포함.

---

## 0) 컨텍스트 요약

* **프로젝트명**: BLOOMI
* **우선순위**: 백엔드부터(프론트는 간단 폼으로 추후)
* **핵심 기능(Phase 1)**: 이미지(필수), 음식 이름(선택), 중량/용량(선택)을 입력받아 칼로리 추정 결과를 반환하는 API. DB 없이 시작.
* **프론트**: React Native(카메라 촬영과 갤러리 업로드 지원).
* **백엔드 선호 스택**: Java + Spring Boot. (사용자는 DDD 지향, 테스트 경험 적음)
* **향후**: JWT 인증 도입 예정, 일정/캘린더·통계 기능 확장, 추후 DB 도입.

---

## 1) 역할/범위

* **이 문서 사용 대상**: 백엔드 개발자 + AI 모델(Claude/ChatGPT)
* **목표 산출물**:

  1. Spring Boot 기반의 최소 기능 API 서버
  2. Vision 모델 연동(사진 → 음식/중량/칼로리 추정)
  3. 로컬 실행 및 간단 부하테스트 가이드

---

## 2) 시스템 프롬프트(LLM용)

다음 **시스템 지침**을 Claude/ChatGPT에 전달하여 일관된 응답을 유도한다.

```
당신은 BLOOMI 백엔드 팀의 시니어 엔지니어다.
목표는 "이미지 기반 칼로리 추정 API"를 신속하게 구축·개선하는 것이다.

원칙:
1) 실용주의: MVP 우선, DB 없는 설계, 이후 확장 용이하게.
2) DDD 감수성: 컨트롤러-애플리케이션-도메인-인프라 레이어 구분.
3) 테스트: 컨트롤러 슬라이스/애플리케이션 서비스 단위테스트 최소화.
4) 관찰성: 요청 ID/로깅/간단 메트릭 필수.
5) 보안: 키·비밀은 환경변수, 업로드 파일 검증.

출력 규칙:
- 답변은 코드 우선, 실행 지침은 순서대로, 의사결정은 근거 포함.
- 덜 중요한 선택지는 `Trade-off` 섹션에 정리.
- 불확실성은 TODO로 남기고 보수적 디폴트를 제안.
```

---

## 3) 아키텍처 개요

* **패턴**: 레이어드 + DDD 친화 + Port & Adapter (Hexagonal Architecture)
* **흐름**: `Controller → Application(Service) → Domain(Port) → Infra(Adapter → Client)`
* **외부 연동**: OpenAI/Anthropic/Google Vision API 중 택1, 프로바이더 교체 가능한 포트/어댑터 구조.

### 3.1 패키지 구조 (실제 구현)

```
com.han.bloomi
 ├─ api
 │   ├─ controller          (REST API 컨트롤러)
 │   └─ dto                 (API 요청/응답 DTO)
 ├─ application
 │   └─ service             (애플리케이션 서비스/유스케이스)
 ├─ domain
 │   ├─ model               (도메인 모델)
 │   │   ├─ MealAnalysis    (분석 결과)
 │   │   ├─ MealAnalysisRequest (분석 요청)
 │   │   ├─ Macros          (3대 영양소)
 │   │   ├─ Serving         (제공량)
 │   │   └─ FoodItem        (음식 항목)
 │   └─ port
 │       └─ VisionPort      (Vision API 포트 인터페이스)
 ├─ infra
 │   └─ vision
 │       ├─ VisionAdapter   (VisionPort 구현체)
 │       ├─ VisionClient    (클라이언트 공통 인터페이스)
 │       ├─ VisionProvider  (프로바이더 Enum)
 │       ├─ PromptFactory   (프롬프트 생성)
 │       └─ openai
 │           └─ OpenAiVisionClient (OpenAI 구현체)
 └─ common
     ├─ error               (에러 코드/응답)
     ├─ exception           (커스텀 예외)
     └─ trace               (TraceId 관리)
```

### 3.2 Vision API Port & Adapter 패턴

```
                    ┌─────────────────┐
                    │   Controller    │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │   Application   │
                    │     Service     │
                    └────────┬────────┘
                             │
              ┌──────────────▼──────────────┐
              │    Domain Layer (Port)      │
              │                              │
              │  ┌────────────────────────┐ │
              │  │     VisionPort         │ │  ← 인터페이스 (교체 가능)
              │  │  (도메인이 정의)       │ │
              │  └────────────────────────┘ │
              └──────────────┬──────────────┘
                             │ implements
              ┌──────────────▼──────────────┐
              │ Infrastructure Layer         │
              │                              │
              │  ┌────────────────────────┐ │
              │  │   VisionAdapter        │ │  ← 포트 구현체
              │  └──────────┬─────────────┘ │
              │             │ delegates      │
              │  ┌──────────▼─────────────┐ │
              │  │   VisionClient         │ │  ← 클라이언트 인터페이스
              │  └──────────┬─────────────┘ │
              │             │ implements     │
              │  ┌──────────▼─────────────┐ │
              │  │ OpenAiVisionClient     │ │  ← OpenAI 구체 구현
              │  │ AnthropicVisionClient  │ │  ← Anthropic 구체 구현
              │  │ GoogleVisionClient     │ │  ← Google 구체 구현
              │  └────────────────────────┘ │
              └──────────────────────────────┘

```

**핵심 원칙:**
1. **도메인 레이어는 인프라에 의존하지 않음** - VisionPort는 도메인이 정의
2. **인프라 레이어가 도메인에 의존** - VisionAdapter가 VisionPort를 구현
3. **프로바이더 교체 용이** - application.properties에서 `bloomi.vision.provider=openai|anthropic|google` 설정만 변경

---

## 4) API 스펙 (v1)

### 4.1 `POST /api/v1/meal/analyze`

* **요청**: `multipart/form-data`

  * `image` (file, required): 음식 사진
  * `name` (string, optional): 사용자가 입력한 음식명(있으면 힌트로 활용)
  * `weight` (number, optional): 중량(g) 또는 용량(ml)
  * `notes` (string, optional): 기타 설명
* **응답(200)**

```json
{
  "calories": 523.0,
  "macros": {"carbs": 65.3, "protein": 24.1, "fat": 19.8},
  "serving": {"unit": "g", "amount": 350},
  "items": [
    {"name": "닭가슴살", "amount": 200, "unit": "g", "calories": 220},
    {"name": "현미밥", "amount": 150, "unit": "g", "calories": 303}
  ],
  "confidence": 0.78,
  "advice": "단백질 비율이 좋아요. 소금은 줄이세요.",
  "traceId": "2b6f-..."
}
```

* **에러**

  * 400: 입력 유효성 실패(파일 누락/확장자 오류)
  * 415: 미지원 미디어 타입
  * 429: 레이트 리밋 초과
  * 502: LLM/Vision 응답 오류(타임아웃 포함)

### 4.2 헬스체크

* `GET /actuator/health` (Spring Boot Actuator)

---

## 5) DTO/계약

```java
// request
public record AnalyzeMealRequest(String name, Double weight, String notes) {}

// response
public record AnalyzeMealResponse(
    double calories,
    Macros macros,
    Serving serving,
    List<Item> items,
    double confidence,
    String advice,
    String traceId
) {
  public record Macros(double carbs, double protein, double fat) {}
  public record Serving(String unit, double amount) {}
  public record Item(String name, double amount, String unit, double calories) {}
}
```

---

## 6) 컨트롤러(예시)

```java
@RestController
@RequestMapping("/api/v1/meal")
@RequiredArgsConstructor
public class MealAnalyzeController {
  private final MealAnalyzeService service;

  @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public AnalyzeMealResponse analyze(
      @RequestPart("image") MultipartFile image,
      @RequestPart(value = "payload", required = false) AnalyzeMealRequest payload
  ) {
    return service.analyze(image, payload);
  }
}
```

> 프런트가 단순하면 `name`, `weight`를 각각 `@RequestParam`으로 받아도 됨. 위 예시는 JSON 서브파트(`payload`)를 허용.

---

## 7) 애플리케이션 서비스(예시)

```java
@Service
@RequiredArgsConstructor
public class MealAnalyzeService {
  private final VisionPort vision;
  private final TraceIdHolder trace;

  public AnalyzeMealResponse analyze(MultipartFile image, AnalyzeMealRequest req) {
    String traceId = trace.current();
    VisionResult vr = vision.infer(image, req);
    return VisionMapper.toResponse(vr, traceId);
  }
}
```

---

## 8) 도메인 포트 & 인프라 어댑터

```java
public interface VisionPort {
  VisionResult infer(MultipartFile image, AnalyzeMealRequest hint);
}
```

```java
@Component
@RequiredArgsConstructor
public class OpenAiVisionClient implements VisionPort {
  private final OpenAiHttp http;
  private final PromptFactory promptFactory;

  @Override
  public VisionResult infer(MultipartFile image, AnalyzeMealRequest hint) {
    String prompt = promptFactory.forCalorie(hint);
    // 1) 이미지 → base64 (또는 프리사인 URL)
    // 2) OpenAI Responses API 호출 (model: gpt-4.1/omni 계열)
    // 3) JSON 스키마 강제 → VisionResult 매핑
    return http.callVision(prompt, image);
  }
}
```

---

## 9) 프롬프트(칼로리 추정용 템플릿)

```
당신은 영양 성분 분석가입니다. 목표:
- 음식 사진과 힌트(name, weight)가 주어지면 1회 섭취 기준 칼로리와 3대 영양소를 추정해 JSON으로만 출력.
- 확신도(confidence)를 0~1 사이로 제공.
- weight가 주어지면 이를 우선 고려, 없을 경우 일반적인 1인분 기준을 가정하고 serving을 명시.

JSON 스키마:
{
  "calories": number,
  "macros": {"carbs": number, "protein": number, "fat": number},
  "serving": {"unit": "g"|"ml", "amount": number},
  "items": [{"name": string, "amount": number, "unit": string, "calories": number}],
  "confidence": number,
  "advice": string
}
```

---

## 10) 구성/환경 변수

* `OPENAI_API_KEY` (필수)
* `OPENAI_BASE_URL` (선택, 프록시나 전용 게이트웨이 사용 시)
* `HTTP_TIMEOUT_MS` (기본 20_000)
* `MAX_IMAGE_SIZE_MB` (기본 10)
* `ALLOW_ORIGINS` (CORS)

---

## 11) 비기능 요구사항

* **로깅**: traceId(MDC) + 요청/응답 요약(민감정보 제외, 이미지 비로그)
* **레이트리밋**: IP/토큰 기준 간단 제한(예: Bucket4j)
* **검증**: 이미지 확장자/용량 체크, MIME 스니핑
* **관찰성**: Actuator + `/metrics`, `/health`
* **에러 포맷**:

```json
{"code":"VISION_TIMEOUT","message":"Provider timeout","traceId":"..."}
```

---

## 12) 로컬 실행 가이드

1. JDK 21, Gradle 설치
2. `OPENAI_API_KEY` 설정
3. `./gradlew bootRun`
4. cURL 테스트

```
curl -X POST http://localhost:8080/api/v1/meal/analyze \
  -F image=@/path/meal.jpg \
  -F name="닭가슴살 샐러드" \
  -F weight=350
```

---

## 13) 간단 부하 테스트(JMeter 방향)

* 샘플 스레드 그룹: 동시 5~20, 루프 10, Think Time 1~3s
* Multipart 업로드 샘플러 + 정적 이미지 3장 로테이션
* 관측지표: p95, 에러율, 외부 API 대기시간

---

## 14) 확장 로드맵

* **Auth**: JWT(Access/Refresh), 앱키 발급
* **DB 도입**: 요청/응답 메타 저장, 사용자/기기/히스토리
* **영양 DB 연동**: USDA/FDC 또는 국내 DB와의 교차보정
* **캘린더/통계**: 식단 기록, 주별 합계/영양 밸런스 리포트
* **코스트 제어**: 프롬프트 압축, 비전 샷 수 최소화, 캐싱

---

## 15) 결정 & 트레이드오프

* **DB 미도입**: 속도/단순성 ↑, 그러나 히스토리/리포트 기능은 제한 → v2에서 DB 추가
* **프로바이더 고정(OpenAI 우선)**: 초기 복잡도↓, 단 포터블 포트/어댑터로 교체 용이성 확보
* **정밀도 vs 비용**: 이미지 1장 1콜 디폴트, 재시도는 1회 제한

---

## 16) 체크리스트

* [ ] `/api/v1/meal/analyze` 동작 확인
* [ ] 400/415/429/502 케이스 테스트
* [ ] .env 예시 제공
* [ ] README 최소화(시작법 + API 예시)
* [ ] 샘플 이미지 2~3장 포함(로컬)

---

## 17) 부록: 예시 build.gradle.kts 스니펫

```kotlin
dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("io.github.bucket4j:bucket4j-core:8.9.0")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

---

## 18) 부록: 에러 처리 구조 (실제 구현)

### 18.1 패키지 구조

```
com.han.bloomi.common
 ├─ error
 │   ├─ ErrorCode.java          (에러 코드 정의)
 │   ├─ ErrorResponse.java      (통일된 에러 응답)
 │   └─ GlobalExceptionHandler.java
 ├─ exception
 │   ├─ BusinessException.java  (비즈니스 예외 기본)
 │   └─ VisionException.java    (Vision API 전용)
 └─ trace
     ├─ TraceIdHolder.java       (TraceId 생성/관리)
     └─ TraceIdFilter.java       (요청 필터)
```

### 18.2 ErrorCode (Enum)

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "Invalid input provided"),
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", "Invalid image format"),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE", "Image size exceeds limit"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA", "Unsupported media type"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", "Rate limit exceeded"),
    VISION_TIMEOUT(HttpStatus.BAD_GATEWAY, "VISION_TIMEOUT", "Provider timeout"),
    // ...

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
```

### 18.3 ErrorResponse (Record)

```java
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    String traceId,
    String detail
) {
    public static ErrorResponse of(ErrorCode errorCode, String traceId) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .traceId(traceId)
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String traceId, String detail) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .traceId(traceId)
                .detail(detail)
                .build();
    }
}
```

### 18.4 예외 클래스 계층

```java
// 비즈니스 예외 기본 클래스
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }
}

// Vision API 전용 예외
public class VisionException extends BusinessException {
    public VisionException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
```

### 18.5 GlobalExceptionHandler (AOP 기반)

```java
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final TraceIdHolder traceIdHolder;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        String traceId = traceIdHolder.current();
        log.error("[{}] BusinessException: {}", traceId, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), traceId, ex.getDetail());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        String traceId = traceIdHolder.current();
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.PAYLOAD_TOO_LARGE, traceId);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        String traceId = traceIdHolder.current();
        log.error("[{}] Unhandled exception", traceId, ex);
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
```

### 18.6 TraceId 관리 (MDC 기반)

```java
@Component
public class TraceIdHolder {
    private static final String TRACE_ID_KEY = "traceId";

    public String current() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null) {
            traceId = generate();
            MDC.put(TRACE_ID_KEY, traceId);
        }
        return traceId;
    }

    public String generate() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

@Component
@RequiredArgsConstructor
public class TraceIdFilter extends OncePerRequestFilter {
    private final TraceIdHolder traceIdHolder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            String traceId = request.getHeader("X-Trace-Id");
            if (traceId == null) traceId = traceIdHolder.generate();
            traceIdHolder.set(traceId);
            response.setHeader("X-Trace-Id", traceId);
            filterChain.doFilter(request, response);
        } finally {
            traceIdHolder.clear();
        }
    }
}
```

### 18.7 사용 예시

```java
// 서비스나 컨트롤러에서 간단하게 throw
throw new BusinessException(ErrorCode.INVALID_IMAGE_FORMAT);

// 상세 정보 포함
throw new BusinessException(ErrorCode.VISION_TIMEOUT, "OpenAI API timeout after 30s");

// Vision 전용
throw new VisionException(ErrorCode.VISION_API_ERROR, "Invalid API response");
```

**자동 응답 생성:**
```json
{
  "code": "VISION_TIMEOUT",
  "message": "Provider timeout",
  "traceId": "2b6f-a3c1",
  "detail": "OpenAI API timeout after 30s"
}
```

**특징:**
- ✅ Spring AOP (`@RestControllerAdvice`)로 모든 예외 자동 처리
- ✅ TraceId 자동 생성 및 MDC 통합 (로그 추적)
- ✅ HTTP 헤더 `X-Trace-Id` 자동 설정
- ✅ 통일된 에러 응답 포맷
- ✅ 코드에서는 단순 throw만 수행

---

## 19) 오픈 질문(TODO)

* [ ] 초기 모델: OpenAI Omni vs 다른 Vision? 비용/정확도 비교 필요
* [ ] 다국어 라벨(한국어/영어) 동시 지원 범위
* [ ] 중량 추정 로직(그램 자동 추정 vs 사용자 입력 우선 순위)
* [ ] 프런트 카메라 EXIF 회전 보정 고려