# PreService 민감정보 암호화 설계 설명

## 1. 한 줄 요약

Spring Boot에서 컨트롤러가 업무 로직을 실행하기 전에 요청 DTO의 민감정보 필드를 자동으로 암호화하는 공통 전처리 기능이다.

---

## 2. 문제 상황

일반적으로 API에서 민감정보를 받으면 컨트롤러나 서비스 코드에서 직접 암호화할 수 있다.

```java
request.setCustRrn(encrypt(request.getCustRrn()));
```

하지만 이 방식에는 다음 문제가 있다.

- API마다 암호화 코드가 반복된다.
- 개발자가 특정 필드 암호화를 누락할 수 있다.
- 암호화 정책이 바뀌면 여러 업무 코드를 수정해야 한다.
- 민감정보 처리라는 보안 관심사가 업무 로직 안에 섞인다.

그래서 민감정보 처리를 업무 코드 밖으로 빼서 공통 기능으로 만들었다.

---

## 3. 전체 구조

```mermaid
flowchart TD
    A[Client Request] --> B[Spring MVC]
    B --> C[JSON Body를 DTO 객체로 변환]
    C --> D[PreServiceSensitiveFieldHandler]
    D --> E[SensitiveFieldProcessor]
    E --> F{@Sensitive 필드 탐색}
    F --> G[AES-GCM 암호화]
    G --> H[Controller 호출]
    H --> I[Service 실행]
```

---

## 4. Spring에서 요청이 처리되는 방식

클라이언트가 JSON 요청을 보낸다.

```json
{
  "inquiryId": 1,
  "pdId": 1,
  "lnAmt": 30000000,
  "custRrn": "9001011000000"
}
```

Spring MVC는 이 JSON을 자바 객체로 바꾼다.

```java
public class LoanApplyRequest {
    private Long inquiryId;
    private Long pdId;
    private BigDecimal lnAmt;
    private String custRrn;
}
```

컨트롤러는 이렇게 객체를 받는다.

```java
@PostMapping
public LoanApplyResponse apply(@RequestBody LoanApplyRequest request) {
    ...
}
```

여기서 `@RequestBody`는 HTTP 요청 body의 JSON을 자바 객체로 변환해서 넣어달라는 뜻이다.

---

## 5. RequestBodyAdvice

`RequestBodyAdvice`는 Spring MVC가 제공하는 확장 지점이다.

쉽게 말하면 다음 위치에 끼어들 수 있는 기능이다.

> JSON 요청 body가 자바 객체로 변환된 직후, 컨트롤러 메서드로 들어가기 전

이번 기능에서는 이 지점을 사용했다.

```java
@ControllerAdvice
public class PreServiceSensitiveFieldHandler extends RequestBodyAdviceAdapter {

    @Override
    public Object afterBodyRead(Object body, ...) {
        return sensitiveFieldProcessor.processForStorage(body);
    }
}
```

`afterBodyRead`는 요청 body가 DTO로 변환된 직후 실행된다.

```text
JSON
↓
LoanApplyRequest 객체 생성
↓
afterBodyRead 실행
↓
Controller 호출
```

그래서 DTO 필드에 붙은 어노테이션을 읽고 암호화하기 좋다.

---

## 6. 왜 Filter나 Interceptor가 아니라 RequestBodyAdvice인가

Spring에는 요청 중간에 끼어들 수 있는 기술이 여러 개 있다.

| 기술 | 실행 위치 | 이번 기능에 적합한가 |
|---|---|---|
| `Filter` | Spring MVC 이전, HTTP 요청 수준 | JSON이 아직 DTO가 아니라 필드 처리 어려움 |
| `HandlerInterceptor` | 컨트롤러 호출 전 | body 객체 접근이 어색함 |
| `AOP` | 메서드 실행 전후 | 컨트롤러 인자 접근은 가능하지만 MVC body 변환 흐름과는 거리 있음 |
| `RequestBodyAdvice` | body가 DTO로 변환된 직후 | 가장 적합 |

이번 기능은 필드에 붙은 `@Sensitive`를 보고 암호화해야 한다. 그래서 JSON 문자열이 아니라 자바 객체가 된 직후가 가장 적절하다.

---

## 7. @ControllerAdvice

`@ControllerAdvice`는 여러 컨트롤러에 공통으로 적용되는 Spring 기능이다.

일반 컨트롤러는 특정 API 요청을 처리한다.

```java
@RestController
public class LoanController {
}
```

반면 `@ControllerAdvice`는 여러 컨트롤러 주변에서 공통 처리를 한다.

```java
@ControllerAdvice
public class PreServiceSensitiveFieldHandler {
}
```

예를 들어 다음 기능들이 여기에 자주 들어간다.

- 공통 예외 처리
- 응답 변환
- 요청 body 전처리
- 응답 body 후처리

이번에는 요청 body 전처리에 사용했다.

---

## 8. @Sensitive 어노테이션

민감정보 필드에는 직접 만든 어노테이션을 붙인다.

```java
@Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
private String custRrn;
```

이 뜻은 다음과 같다.

> 이 필드는 민감정보이고, 저장/전달 전에 암호화해야 한다.

업무 개발자는 암호화 코드를 직접 호출하지 않는다. 필드에 정책만 선언한다.

---

## 9. StoragePolicy

민감정보를 어떻게 처리할지 나타내는 정책이다.

```java
public enum StoragePolicy {
    PLAIN,    // 원문 유지: "홍길동" -> "홍길동"
    MASK,     // 마스킹: "01012345678" -> "010****5678"
    ENCRYPT,  // 암호화: "9001011000000" -> "ENC(q83b...)"
    HASH,     // 해시: "9001011000000" -> "HASH(4f8c2a9e...)"
    DROP      // 저장 제외: "123456" -> null 또는 저장 대상 제외
}
```

현재 구현한 것은 `ENCRYPT`다. 나머지는 향후 응답 마스킹, 검색용 해시, 거래저널 저장 제외 같은 기능으로 확장할 수 있다.

---

## 10. SensitiveFieldProcessor

`SensitiveFieldProcessor`는 실제로 필드를 찾아 처리하는 클래스다.

역할은 다음과 같다.

1. 요청 DTO 객체를 받는다.
2. 리플렉션으로 필드를 순회한다.
3. `@Sensitive`가 붙은 필드를 찾는다.
4. 정책이 `ENCRYPT`면 암호화한다.
5. 암호화된 값을 다시 DTO 필드에 넣는다.

리플렉션은 실행 중에 클래스의 필드나 어노테이션 정보를 읽는 기술이다.

예를 들어 이런 클래스가 있으면:

```java
public class LoanApplyRequest {
    @Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
    private String custRrn;
}
```

실행 중에 `custRrn` 필드에 `@Sensitive`가 붙어 있는지 확인할 수 있다.

---

## 11. 암호화 방식: AES-GCM

이번 구현은 AES-GCM을 사용한다.

```text
AES/GCM/NoPadding
```

쉽게 말하면:

- `AES`: 데이터를 암호화하는 표준 알고리즘
- `GCM`: 암호화와 변조 검증을 함께 제공하는 모드

처리 결과는 이런 형태다.

```text
9001011000000
↓
ENC(q83b...base64...)
```

또한 매번 랜덤 IV를 생성하기 때문에 같은 값을 암호화해도 결과가 달라진다.

```text
9001011000000 -> ENC(abc...)
9001011000000 -> ENC(xyz...)
```

이 동작은 정상이며, 같은 원문이 같은 암호문으로 반복 노출되는 것을 막는다.

---

## 12. AutoConfiguration

Spring Boot의 AutoConfiguration은 의존성만 추가하면 필요한 Bean을 자동 등록하는 기능이다.

업무 서비스가 매번 이런 설정을 직접 하지 않아도 된다.

```java
@Bean
TextEncryptor textEncryptor() {
    return new AesGcmTextEncryptor(...);
}
```

대신 공통 모듈에서 자동 설정을 제공한다.

```java
@AutoConfiguration
public class BankCommonAutoConfiguration {

    @Bean
    TextEncryptor textEncryptor(...) {
        return new AesGcmTextEncryptor(...);
    }

    @Bean
    SensitiveFieldProcessor sensitiveFieldProcessor(TextEncryptor textEncryptor) {
        return new SensitiveFieldProcessor(textEncryptor);
    }

    @Bean
    PreServiceSensitiveFieldHandler preServiceSensitiveFieldHandler(
            SensitiveFieldProcessor sensitiveFieldProcessor) {
        return new PreServiceSensitiveFieldHandler(sensitiveFieldProcessor);
    }
}
```

그리고 자동 설정 등록 파일에 추가한다.

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

```text
com.bank.common.core.autoconfigure.BankCommonAutoConfiguration
```

그러면 `loan-svc`는 의존성만 추가하면 된다.

```gradle
dependencies {
    implementation project(':bank-common-core')
}
```

---

## 13. Bean과 의존성 주입

Spring에서 Bean은 Spring 컨테이너가 관리하는 객체다.

직접 `new`로 만들지 않고, Spring이 생성하고 필요한 곳에 주입해준다.

```java
public PreServiceSensitiveFieldHandler(SensitiveFieldProcessor sensitiveFieldProcessor) {
    this.sensitiveFieldProcessor = sensitiveFieldProcessor;
}
```

이 생성자에 들어가는 `SensitiveFieldProcessor`를 Spring이 알아서 넣어준다. 이걸 의존성 주입이라고 한다.

---

## 14. 모듈 분리

이번 구현은 두 모듈로 나눴다.

```text
bank-common
bank-common-core
```

`bank-common`은 Spring을 모르는 순수 Java 모듈이다.

```text
@Sensitive
StoragePolicy
SensitiveFieldProcessor
TextEncryptor
AesGcmTextEncryptor
```

`bank-common-core`는 Spring과 연결하는 모듈이다.

```text
PreServiceSensitiveFieldHandler
BankCommonAutoConfiguration
```

이렇게 나누면 다음 장점이 있다.

- 암호화/필드 처리 로직은 Spring 없이 테스트 가능하다.
- Spring MVC 연결부만 별도로 관리할 수 있다.
- 나중에 batch, kafka consumer 같은 Spring MVC가 아닌 환경에서도 `bank-common`은 재사용할 수 있다.

---

## 15. 실제 대출 실행 적용 흐름

요청 DTO:

```java
public class LoanApplyRequest {
    private Long inquiryId;
    private Long pdId;
    private BigDecimal lnAmt;

    @Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
    private String custRrn;
}
```

클라이언트 요청:

```json
{
  "inquiryId": 1,
  "pdId": 1,
  "lnAmt": 30000000,
  "custRrn": "9001011000000"
}
```

컨트롤러 진입 전:

```text
custRrn = "ENC(q83b...base64...)"
```

컨트롤러:

```java
new LoanApplyCommand(
    request.getInquiryId(),
    request.getPdId(),
    request.getLnAmt(),
    request.getCustRrn()
)
```

서비스:

```java
new LnArrCreateSpec(
    inquiry.getCustId(),
    result.getPdId(),
    command.getLnAmt(),
    result.getIntrRt(),
    today,
    today.plusYears(1),
    command.getCustRrn()
)
```

이 흐름에서 컨트롤러와 서비스는 원문 주민등록번호를 직접 다루지 않는다.

---

## 16. 이 설계의 핵심 장점

### 16.1 업무 코드 단순화

암호화 호출 코드가 컨트롤러/서비스에 흩어지지 않는다.

### 16.2 누락 가능성 감소

필드에 `@Sensitive`만 선언하면 공통 처리된다.

### 16.3 정책 변경 용이

AES-GCM에서 다른 암호화 방식으로 바꾸더라도 공통 모듈만 수정하면 된다.

### 16.4 재사용성

`bank-common-core`를 의존하는 다른 서비스에도 적용 가능하다.

### 16.5 테스트 용이성

순수 Java 로직과 Spring 연동 로직을 분리했다.

---

## 17. 주의할 점

### 17.1 DTO 값 직접 변경

현재 방식은 DTO 값을 직접 바꾼다.

컨트롤러 이후에는 원문 값을 사용할 수 없다. 이 동작이 의도된 정책인지 검토해야 한다.

### 17.2 적용 범위

`RequestBodyAdvice`는 request body에만 적용된다.

다음 값들은 별도 처리 필요하다.

- query parameter
- path variable
- header
- multipart
- form-urlencoded

### 17.3 Bean Validation 순서

주민번호 형식 검증이 있다면 암호화 전에 검증되어야 한다.

예를 들어 아래 검증이 있다면:

```java
@Pattern(regexp = "\\d{13}")
private String custRrn;
```

암호화 후에는 `ENC(...)` 형태가 되므로 검증 순서를 확인해야 한다.

### 17.4 개발 환경 마스킹 해제

개발 환경에서 마스킹 해제를 허용하더라도 암호화는 유지하는 것이 안전하다.

### 17.5 키 관리

현재는 property 기반 키를 사용하지만, 운영에서는 Vault/KMS/HSM 같은 키 관리 체계가 필요하다.

---

## 18. 멘토링에서 같이 이야기하면 좋은 질문

- 요청 DTO를 직접 암호화된 값으로 바꾸는 방식이 적절한가?
- 저장 정책, 응답 마스킹 정책, 거래저널 정책을 하나의 enum으로 관리해도 되는가?
- AES-GCM 암호문에 key version을 넣어 key rotation을 지원해야 하는가?
- 개발 환경에서 마스킹 해제는 어디까지 허용해야 하는가?
- `RequestBodyAdvice` 외에 더 적절한 Spring 확장 지점이 있는가?
- 누락된 `@Sensitive` 필드를 어떻게 탐지할 수 있을까?

---

## 19. 정리

이 설계는 민감정보 암호화를 개별 업무 코드에서 직접 처리하지 않고, Spring MVC 요청 처리 흐름의 공통 전처리 단계로 분리한 구조다.

`@Sensitive` 어노테이션으로 정책을 선언하고, `RequestBodyAdvice`와 AutoConfiguration을 통해 여러 서비스에 반복 적용할 수 있도록 만들었다.
