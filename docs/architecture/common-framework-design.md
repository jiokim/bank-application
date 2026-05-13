# 금융 공통 처리 구조 Spring Boot 재구현 설계

기존 금융 공통 프레임워크의 CmnContext, Pre/Post Handler, 거래저널, 민감정보 암호화/마스킹 구조를 Spring Boot 기반으로 재해석한 설계 문서다. 면접에서 "금융 공통 프레임워크의 처리 구조를 Spring Boot 기반으로 재해석해 직접 구현했다"고 설명할 수 있는 설계 경험을 목표로 한다.

## 설계 목표

- 요청 단위 식별자(GUID)와 채널/기관/서비스 정보를 공통 컨텍스트로 관리
- 요청마다 거래저널을 자동 기록해 추적 가능성 확보
- 민감정보를 필드 단위 어노테이션으로 선언적으로 처리
- 채널과 권한 기준으로 응답 마스킹 차등 적용
- 저장 구현체(DB, Kafka, Elasticsearch)를 교체 가능한 인터페이스 구조

## 모듈 구조

기존 `-api` / `-core` 분리 컨벤션을 따른다.

| 모듈 | 역할 |
|---|---|
| `bank-common` | 순수 Java. BankRequestContext, TransactionJournal, TransactionJournalWriter(인터페이스), @Sensitive, StoragePolicy |
| `bank-common-core` | Spring 의존. Filter, Aspect, Advice, DbWriter, AutoConfiguration |

`loan-svc`, `product-svc`는 `bank-common-core` 의존 추가만으로 공통 기능이 자동 적용된다.

## 컴포넌트 설계

### 1. BankRequestContext

요청 단위 컨텍스트. ThreadLocal로 전파한다.

```java
public class BankRequestContext {
    private String guid;
    private String institutionCode;
    private String channelCode;
    private String userId;
    private String serviceCode;
    private String requestUri;
    private String httpMethod;
}

public final class BankRequestContextHolder {
    private static final ThreadLocal<BankRequestContext> holder = new ThreadLocal<>();

    public static void set(BankRequestContext ctx) { holder.set(ctx); }
    public static BankRequestContext get() { return holder.get(); }
    public static void clear() { holder.remove(); }
}
```

### 2. BankRequestContextFilter

`OncePerRequestFilter` 기반. GUID 생성 또는 헤더 수신, MDC 저장.

- 외부 채널(인터넷)의 `X-Request-ID`는 서버에서 신규 생성, 클라이언트 값은 `parentGuid`로만 기록
- `finally` 블록에서 반드시 `BankRequestContextHolder.clear()` 호출 (ThreadLocal 누수 방지)

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain chain) throws IOException, ServletException {
    BankRequestContext ctx = buildContext(request);
    BankRequestContextHolder.set(ctx);
    MDC.put("guid", ctx.getGuid());
    MDC.put("channelCode", ctx.getChannelCode());
    MDC.put("serviceCode", ctx.getServiceCode());
    try {
        chain.doFilter(request, response);
    } finally {
        BankRequestContextHolder.clear();
        MDC.clear();
    }
}
```

### 3. @Sensitive + StoragePolicy

필드 단위 민감정보 처리 정책을 선언적으로 정의한다.

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
    StoragePolicy storagePolicy() default StoragePolicy.MASK;
    MaskingPolicy maskingPolicy() default MaskingPolicy.PARTIAL;
}

public enum StoragePolicy {
    PLAIN,    // 원문 저장
    MASK,     // 마스킹 후 저장
    ENCRYPT,  // 암호화 후 저장
    HASH,     // 해시 후 저장
    DROP      // 저장 제외
}
```

`SensitiveFieldProcessor`가 리플렉션으로 필드를 순회해 정책을 적용한다. 거래저널 저장 전에 반드시 처리한다.

### 4. TransactionJournalAspect

`@within(RestController)` 포인트컷으로 컨트롤러 메서드만 대상으로 한다. AOP에서 Writer를 직접 호출하지 않고 `ApplicationEventPublisher`로 이벤트를 발행해 저장 로직과 분리한다.

```java
@Around("@within(org.springframework.web.bind.annotation.RestController)")
public Object journal(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.currentTimeMillis();
    try {
        Object result = pjp.proceed();
        eventPublisher.publishEvent(buildSuccessJournal(pjp, result, start));
        return result;
    } catch (Exception e) {
        eventPublisher.publishEvent(buildFailureJournal(pjp, e, start));
        throw e;
    }
}
```

`@TransactionalEventListener`로 구독해 비동기 저장이 가능하다.

### 5. TransactionJournalWriter

저장 구현체를 교체 가능하도록 인터페이스로 분리한다.

```java
public interface TransactionJournalWriter {
    void write(TransactionJournal journal);
}
```

기본 구현체는 `DbTransactionJournalWriter`. 이후 `KafkaTransactionJournalWriter`, `ElasticsearchTransactionJournalWriter`로 교체 가능.

### 6. ResponseMaskingAdvice

`ResponseBodyAdvice<T>`로 응답 직렬화 시점에 채널코드와 권한 기준으로 마스킹을 적용한다.

- `SensitiveFieldProcessor`와 역할 구분:
  - `SensitiveFieldProcessor` → 저장(거래저널) 시 필드 처리 담당
  - `ResponseMaskingAdvice` → 응답 시 채널/권한 기반 마스킹 담당

## 주의사항

### ThreadLocal 비동기 전파

`CompletableFuture` / `@Async` 사용 시 자식 스레드에서 context가 유실된다. 현재 `loan-svc`의 병렬 한도조회(`POST /inquiries`)가 해당된다.

```java
BankRequestContext ctx = BankRequestContextHolder.get();
CompletableFuture.supplyAsync(() -> {
    BankRequestContextHolder.set(ctx);
    try { return productClient.inquire(pdId); }
    finally { BankRequestContextHolder.clear(); }
});
```

### 암호화 키 관리

개인 프로젝트에서는 `application.yml`에 키를 두더라도 실무에서는 Vault/KMS 연동이 필요하다는 점을 설명할 수 있어야 한다.

## 구현 우선순위

1. `BankRequestContext` + `OncePerRequestFilter` + MDC 설정 → structured logging 완성
2. `TransactionJournalAspect` + `DbTransactionJournalWriter` → 거래 추적 완성
3. `@Sensitive` + `SensitiveFieldProcessor` → 저장 전 마스킹/암호화
4. `ResponseMaskingAdvice` → 채널별 응답 마스킹

## 이력서 표현

```
금융 공통 프레임워크(CmnContext, Pre/Post Handler, 거래저널)의
처리 구조를 Spring Boot로 재해석하여 직접 구현

- OncePerRequestFilter 기반 BankRequestContext로 GUID·채널코드·서비스코드를
  ThreadLocal에 전파, MDC 연동으로 분산 로그 추적 체계 구성
- @Sensitive 어노테이션과 StoragePolicy(MASK/ENCRYPT/HASH/DROP)로
  필드 단위 민감정보 처리 정책을 선언적으로 정의
- AOP + ApplicationEvent 기반 거래저널 파이프라인으로 요청·응답·처리시간·예외를
  서비스 코드 침투 없이 기록, TransactionJournalWriter 인터페이스 분리로
  DB → Kafka 전환 가능하도록 설계
- ResponseBodyAdvice에서 채널코드·권한 기반 마스킹 매트릭스를 구현해
  동일 API가 채널에 따라 차등된 민감도로 응답하도록 처리
```
