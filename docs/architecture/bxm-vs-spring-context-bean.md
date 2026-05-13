# BXM CbbApplicationContext vs Spring @RequestScope

## 배경

BXM(뱅크웨어글로벌 프레임워크)은 요청 컨텍스트(직원ID, 거래일자, 채널코드 등)를 `CmnContext`라는 객체에 담아 어디서든 꺼낼 수 있도록 `CbbApplicationContext`라는 static 유틸리티를 제공한다.

이는 기술을 BXM이 발명한 게 아니라, **Lazy Initialization**과 **Service Locator** 두 패턴을 조합해 프레임워크에 내장한 것이다.

## 패턴 기원

| 패턴 | 기원 |
|------|------|
| Lazy Initialization | Java 초기부터 존재한 패턴 (`if (x == null) x = new X()`) |
| Service Locator | Martin Fowler, 2004 — DI의 대안으로 소개 ([원문](https://martinfowler.com/articles/injection.html)) |

Spring 자체도 Service Locator를 지원한다:
```java
@Autowired
private ApplicationContext context;

context.getBean(SomeService.class);  // Spring의 Service Locator
```

---

## BXM이 Service Locator를 선택한 이유

BXM이 등장한 시대적 맥락을 고려해야 한다.

- **Spring이 보편화되기 전**: BXM은 2000년대 초중반 국내 은행 시스템을 위해 설계됐다. 당시 DI 컨테이너는 낯선 개념이었고, Spring도 막 자리를 잡아가던 시기였다.
- **프레임워크 독립성**: BXM은 자체 트랜잭션 관리, 로깅, 컨텍스트 전파를 직접 구현해야 했다. Spring에 의존할 수 없으니 Service Locator가 현실적인 선택이었다.
- **개발자 진입 장벽 최소화**: 금융권 SI 환경에서는 Spring 숙련도가 균일하지 않다. `CbbApplicationContext.getBean()`은 어디서든, 누구나 같은 방식으로 쓸 수 있다는 장점이 있었다.
- **레거시 코드와의 호환**: static 유틸은 일반 POJO, 유틸 클래스, EJB 등 Spring 컨텍스트 밖에서도 호출 가능하다. DI만으로는 이를 지원하기 어렵다.

---

## BXM 방식의 장단점

**장점**
- 호출 위치 제약 없음 — Spring Bean이 아닌 일반 클래스에서도 호출 가능
- 코드가 단순함 — 한 줄로 어디서든 컨텍스트 접근
- Spring 버전·설정에 무관하게 동작

**단점**
- 의존성이 숨겨짐 — 생성자/필드에 드러나지 않아 무엇에 의존하는지 파악하기 어려움
- 테스트 어려움 — static 호출이라 Mock 주입이 사실상 불가능, 통합 컨텍스트 없으면 테스트 자체가 안 됨
- 초기화 순서 문제 — static이라 Spring이 제어하지 않는 임의 시점(static initializer, 다른 Bean 생성자 내부 등)에서도 호출 가능. 문제가 터지는 위치가 예측 불가능하고 디버깅이 어려움. Spring DI도 컨테이너 없이는 동작하지 않지만, 순환 의존은 시작 시점에 `BeanCurrentlyInCreationException`으로 즉시 잡아주는 것과 대조됨
- 멀티스레드 안전성을 직접 보장해야 함 — ThreadLocal 관리를 프레임워크가 직접 책임짐

```java
// BXM — 어디서든 static으로 꺼냄
CmnContext ctx = CbbApplicationContext.getBean(CmnContext.class);
String staffId = ctx.getStaffId();
```

---

## Spring @RequestScope

요청마다 달라지는 정보는 `@RequestScope` Bean으로 등록한다. Spring이 프록시를 통해 요청별 격리를 보장하지만, 이 Bean을 필요로 하는 모든 서비스 레이어에 생성자 주입해야 한다는 부담이 따른다.

**등록:**
```java
@Bean
@RequestScope
public BankRequestContext bankRequestContext(HttpServletRequest request) {
    return new BankRequestContext(
        request.getHeader("X-Staff-Id"),
        request.getHeader("X-Channel-Code"),
        LocalDate.now()
    );
}
```

**사용 — 생성자 주입:**
```java
@Service
@RequiredArgsConstructor
public class LoanApplicationService {
    private final BankRequestContext requestContext;  // 요청별 인스턴스 자동 주입
}
```

`@RequestScope`는 실제 객체 대신 **프록시**를 주입한다. 메서드 호출 시점에 현재 요청의 실제 인스턴스로 연결된다.

**"모든 레이어에 주입해야 한다"는 단점은 과장일 수 있다.** 실제로는 아래 대안도 존재한다:

| 대안 | 설명 |
|------|------|
| `ObjectProvider<BankRequestContext>` | 지연 조회 — 필요한 시점에만 꺼냄 |
| `HandlerMethodArgumentResolver` | Controller 파라미터로 직접 바인딩 |
| AOP / `@Aspect` | 감사 로그처럼 서비스 로직과 완전히 분리 |
| `RequestContextHolder` | Spring 내장 static 접근 — 사실상 Spring의 Service Locator |

---

## 컨텍스트가 진짜 횡단 관심사인가?

"컨텍스트는 횡단 관심사"라는 전제를 맹목적으로 받아들이면 안 된다. 용도에 따라 성격이 달라진다.

| 필드 | 용도 | 성격 |
|------|------|------|
| `staffId`, `channelCode` | 감사 로그, 이력 기록 | 횡단 관심사 — 숨겨도 됨 |
| `channelCode` | 채널별 한도조회 정책 분기 | 업무 입력 — Command에 명시해야 함 |
| `txDate` | 영업일 기준 판단, 금리 적용 | 업무 입력 — Command에 명시해야 함 |
| `staffId` | 직원 권한 검증 | 업무 입력 — 명시적 검증 로직에 포함해야 함 |

업무 판단에 영향을 주는 필드를 컨텍스트에 숨기면 로직을 읽는 사람이 입력이 어디서 오는지 파악할 수 없다. 이 경우 Command 객체에 명시적으로 포함하는 편이 낫다.

---

## ThreadLocal Holder 패턴

Spring Security의 `SecurityContextHolder`, SLF4J의 `MDC`가 이 방식을 쓴다. 단, 둘 다 필터/프레임워크가 생명주기 정리와 전파 전략을 함께 제공한다는 점을 기억해야 한다. static holder만 만든다고 같은 수준의 안전성이 자동으로 따라오지 않는다.

**Holder 클래스 — `get()` 대신 `current()`로 fail-fast:**
```java
public class BankRequestContextHolder {
    private static final ThreadLocal<BankRequestContext> CONTEXT = new ThreadLocal<>();

    public static void set(BankRequestContext ctx) { CONTEXT.set(ctx); }
    public static void clear() { CONTEXT.remove(); }

    public static BankRequestContext current() {
        BankRequestContext ctx = CONTEXT.get();
        if (ctx == null) {
            throw new IllegalStateException("BankRequestContext is not bound to current thread");
        }
        return ctx;
    }
}
```

> `get()`을 그대로 쓰면 설정 누락, 테스트 누락, 비동기 전파 누락이 전부 NPE로 터진다. `current()`처럼 null을 명시적 예외로 변환해야 원인을 추적할 수 있다.

**Interceptor에서 요청 시작 시 세팅:**
```java
@Component
public class BankRequestContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        BankRequestContextHolder.set(new BankRequestContext(
            request.getHeader("X-Staff-Id"),
            request.getHeader("X-Channel-Code"),
            LocalDate.now()
        ));
        return true;
    }

    @Override
    public void afterCompletion(...) {
        BankRequestContextHolder.clear();
    }

    @Override
    public void afterConcurrentHandlingStarted(...) {
        // 비동기 MVC(Callable, DeferredResult)는 요청 스레드가 여기서 반환됨
        // afterCompletion이 아닌 이 시점에 clear해야 pooled thread에 이전 context가 남지 않음
        BankRequestContextHolder.clear();
    }
}
```

**어디서든 꺼내 씀:**
```java
BankRequestContext ctx = BankRequestContextHolder.current();
```

---

## 비동기 경계에서 ThreadLocal은 자동 전파되지 않는다

ThreadLocal Holder의 가장 큰 함정이다. `@Async`, `CompletableFuture`, `TaskExecutor`, 비동기 MVC, 스케줄러 등으로 스레드가 바뀌는 순간 컨텍스트가 끊긴다.

**이 프로젝트의 한도조회는 이미 비동기다:**
```java
// LoanApplicationService — CompletableFuture로 상품별 병렬 조회
List<CompletableFuture<LnInquiryResult>> futures = command.getPdIds().stream()
        .map(pdId -> strategyMap.get(pdId).inquire(...))
        .toList();
```

전략 내부에서 `BankRequestContextHolder.current()`를 호출하면 새 스레드에는 컨텍스트가 없으므로 `IllegalStateException`이 터진다.

**대응 방법 세 가지:**

### 1. TaskDecorator — Executor 레벨에서 전파

```java
@Bean
public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setTaskDecorator(runnable -> {
        BankRequestContext ctx = BankRequestContextHolder.current();
        return () -> {
            BankRequestContextHolder.set(ctx);
            try {
                runnable.run();
            } finally {
                BankRequestContextHolder.clear();
            }
        };
    });
    executor.initialize();
    return executor;
}
```

Executor를 통해 실행되는 모든 작업에 현재 컨텍스트를 자동으로 전달한다. 비동기 코드를 수정하지 않아도 된다.

### 2. Context Snapshot — 호출 지점에서 명시적 캡처

```java
BankRequestContext snapshot = BankRequestContextHolder.current();

CompletableFuture.supplyAsync(() -> {
    BankRequestContextHolder.set(snapshot);
    try {
        return strategy.inquire(...);
    } finally {
        BankRequestContextHolder.clear();
    }
});
```

전파 범위를 명시적으로 제어할 수 있지만, 비동기 코드마다 반복 작성해야 한다.

### 3. Command에 컨텍스트 포함 — 업무 입력이라면 이쪽이 맞다

```java
public record LoanInquiryCommand(
    Long custId,
    List<Long> pdIds,
    String channelCode,   // 채널별 정책이 한도에 영향을 준다면 명시
    LocalDate txDate      // 영업일 기준 금리 산정이라면 명시
) {}
```

컨텍스트 필드가 업무 판단에 직접 영향을 준다면 숨기는 것보다 Command에 포함해 흐름을 명확히 하는 것이 낫다.

---

## 세 가지 방식 최종 비교

| 항목 | BXM `CbbApplicationContext` | Spring `@RequestScope` | ThreadLocal Holder |
|------|----------------------------|------------------------|--------------------|
| 꺼내는 방식 | static Service Locator | 생성자 주입 (DI) | static Holder |
| 의존성 가시성 | 숨겨짐 | 명시적 | 숨겨짐 |
| 요청 격리 | 프레임워크가 관리 | Spring 프록시 자동 격리 | ThreadLocal 격리 |
| 테스트 | 어려움 | Mock 주입 가능 | `set()`으로 직접 세팅 가능 |
| 서비스 코드 오염 | 없음 | 주입 필요 (대안 존재) | 없음 |
| 비동기 전파 | 프레임워크가 관리 | 프록시가 처리 | **자동 전파 안 됨 — 별도 처리 필요** |
| null 안전 | 프레임워크가 관리 | Spring이 보장 | `current()`로 fail-fast 필요 |
| 메모리 누수 위험 | 프레임워크가 관리 | 없음 | `clear()` 누락 / 비동기 MVC 시 발생 가능 |

> ThreadLocal Holder는 동기 요청 처리에서는 실용적인 절충안이다. 단, 비동기 경계가 있다면 TaskDecorator 또는 Command 포함 방식을 함께 결정해야 한다. 이 프로젝트의 한도조회는 `CompletableFuture`를 이미 사용하므로 전파 전략이 필요하다.

## 참고

- Martin Fowler — [Inversion of Control Containers and the Dependency Injection pattern](https://martinfowler.com/articles/injection.html) (2004)
- Spring Security `SecurityContextHolder` — ThreadLocal Holder + 필터 기반 생명주기 관리의 대표 구현체
- SLF4J `MDC` — 로깅 컨텍스트에 동일 패턴 적용, `MDCTaskDecorator`로 비동기 전파 제공
