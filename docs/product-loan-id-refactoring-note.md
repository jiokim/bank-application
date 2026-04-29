# 상품/대출 모듈 ID 타입 정합성 개선 및 빌드 안정화

## 1. 배경

상품, 대출, 약정 모듈을 분리하면서 `product-api`, `product-core`, `product-svc`, `loan-svc` 사이에 상품 ID 타입이 일관되지 않은 상태가 발생했다.

구체적으로 상품 도메인에서는 상품 ID를 문자열(`String`)로 다루는 코드와 숫자(`Long`)로 다루는 코드가 혼재되어 있었다. 반면 약정(`Arr`)과 대출(`LnArr`) 쪽은 이미 `pdId: Long`을 기준으로 설계되어 있었기 때문에, 대출 신청 시 상품 조회와 약정 생성 사이에서 타입 충돌이 발생했다.

이 문제는 단순한 컴파일 오류가 아니라 모듈 경계의 계약이 불명확하다는 신호다. 특히 `-api` 모듈의 인터페이스는 다른 모듈이 의존하는 공개 계약이므로, 여기서 타입이 흔들리면 downstream 모듈 전체가 영향을 받는다.

## 2. 발견된 문제

### 2.1 상품 ID 타입 불일치

`product-api`의 `Pd`는 `String getPdId()`를 제공하고 있었고, `PdMngr.getPd()` 역시 `String productId`를 입력으로 받았다.

반면 `loan-svc`의 대출 신청 요청은 `pdId: Long`을 사용하고 있었고, `loan-core`의 `LnArrCreateSpec`, `arrangement-api`의 `Arr`도 `Long pdId`를 사용하고 있었다.

이로 인해 대출 신청 서비스에서 다음과 같은 문제가 발생했다.

```java
Pd pd = pdMngr.getPd(request.getPdId()); // Long -> String 불일치
```

그리고 상품 조회 후 대출 약정을 생성할 때도 반대 방향의 타입 불일치가 발생했다.

```java
new LnArrCreateSpec(..., pd.getPdId(), ...); // String -> Long 불일치
```

### 2.2 존재하지 않는 `ProductId` VO 참조

`product-svc`의 조회 서비스는 `com.bank.productapi.vo.ProductId`를 사용하고 있었지만, 실제 `product-api` 모듈에는 해당 클래스가 존재하지 않았다.

이는 과거 설계의 잔재가 남았거나, 리팩터링 중 일부 파일만 변경된 상태로 판단된다.

### 2.3 테스트 코드와 실제 계약의 불일치

상품 관련 테스트는 일부는 `Long productId`, 일부는 `String productId`를 기대하고 있었다. 또한 상품 생성 요청에서 현재 필수 값인 `maxLoanAmt`가 누락된 테스트 데이터도 있었다.

테스트가 실제 API 계약을 제대로 반영하지 못하면, 테스트는 회귀 방지 장치가 아니라 오히려 잘못된 설계를 고착시키는 비용이 된다.

### 2.4 자동 구성 클래스 누락

`product-core`에는 Spring Boot auto-configuration imports 파일이 존재했지만, 실제로 참조하는 `ProductAutoConfiguration` 클래스가 없었다.

이 상태에서는 컴파일은 통과하더라도 런타임 자동 구성 단계에서 문제가 발생할 수 있다. 특히 `loan-svc`가 `product-api` 계약을 통해 `PdMngr`를 주입받으려면, `product-core`의 구현체가 애플리케이션 컨텍스트에 안정적으로 등록되어야 한다.

## 3. 의사결정

### 3.1 상품 ID는 `Long`으로 통일

상품 ID 타입은 `Long`으로 통일했다.

판단 근거는 다음과 같다.

- `arrangement-api`의 `Arr.getPdId()`가 이미 `Long`이다.
- `loan-core`의 `LnArrCreateSpec`도 `Long pdId`를 기준으로 한다.
- 대출 신청 API 요청 DTO도 `Long pdId`를 사용하고 있다.
- 현재 in-memory repository는 단순 증가 sequence 기반 ID를 사용하므로 `Long`이 더 자연스럽다.
- 문자열 prefix가 필요한 요구사항이 현재 없다.

초기 구현에서 `"PD1"` 같은 문자열 ID를 만들고 있었지만, 이는 도메인 식별자라기보다 표시용 코드에 가깝다. 향후 상품 코드가 필요해지면 `pdCode` 같은 별도 필드로 분리하는 편이 더 명확하다.

### 3.2 없는 VO는 제거

`ProductId` VO는 현재 코드베이스에 존재하지 않고, 별도의 검증 로직도 제공하지 않는다. 따라서 이번 수정 범위에서는 새로 만들지 않고 제거했다.

VO를 도입하려면 단순 wrapper가 아니라 다음 책임이 있어야 한다.

- 유효한 ID 범위 검증
- 문자열 파싱 정책
- 외부 표현과 내부 표현의 변환
- 잘못된 입력에 대한 명확한 예외 정책

현재는 `Long` 자체로 충분하므로 불필요한 추상화를 추가하지 않았다.

### 3.3 테스트는 현재 API 계약 기준으로 정리

테스트는 구현 세부사항이 아니라 외부 계약을 검증해야 한다. 따라서 product 관련 테스트를 `Long productId`, `maxLoanAmt` 포함 요청, 현재 응답 JSON 구조 기준으로 정리했다.

또한 Spring Boot 4 환경에서 `rest-assured mockmvc 5.5.0`이 Spring MockMvc API와 런타임 시그니처 충돌을 일으켰기 때문에, 해당 acceptance test는 Spring의 `MockMvc`로 변경했다.

이 변경은 테스트 표현력보다 안정성을 우선한 선택이다. 현재 프로젝트는 Spring MVC 기반이므로 `MockMvc`만으로도 컨트롤러-서비스-저장소까지 이어지는 통합 흐름을 충분히 검증할 수 있다.

## 4. 변경 내용

### 4.1 `product-api`

공개 계약을 `Long` 기준으로 변경했다.

```java
public interface Pd {
    Long getPdId();
}
```

```java
public interface PdMngr {
    Pd getPd(Long productId);
}
```

`product-api`는 다른 모듈이 의존하는 계약 모듈이므로, 이 변경이 전체 수정의 기준점이다.

### 4.2 `product-core`

상품 구현체와 저장소를 `Long` ID 기준으로 변경했다.

주요 변경:

- `PdImpl.pdId`: `String` → `Long`
- `ProductRepository.findById`: `String` → `Long`
- `InMemoryProductRepository`의 key 타입: `Map<String, Pd>` → `Map<Long, Pd>`
- 상품 저장 시 `"PD" + sequence` 제거, sequence 값을 그대로 ID로 사용

```java
Long productId = sequence.incrementAndGet();
Pd product = new PdImpl(productId, productName, interestRate, maxLoanAmt);
```

### 4.3 `product-svc`

컨트롤러와 조회 서비스를 `Long` path variable 기준으로 변경했다.

```java
@GetMapping("/v1/products/{productId}")
public ProductResponse getProduct(@PathVariable Long productId) {
    return productQueryService.getProduct(productId);
}
```

`ProductResponse.productId`도 `Long`으로 변경했다.

### 4.4 `product-core` 자동 구성 추가

`ProductAutoConfiguration`을 추가해 `product-core`의 Spring component가 애플리케이션 컨텍스트에 등록될 수 있도록 했다.

```java
@AutoConfiguration
@ComponentScan(basePackages = "com.bank.product")
public class ProductAutoConfiguration {
}
```

현재 구조에서는 `product-core`가 `product-svc`뿐 아니라 `loan-svc`에서도 사용될 수 있다. 따라서 실행 애플리케이션의 base package scan에만 의존하면 모듈 조합에 따라 Bean 등록이 달라질 수 있다.

AutoConfiguration은 이 문제를 줄여준다.

## 5. 검증 결과

전체 테스트를 실행해 컴파일과 테스트를 확인했다.

```bash
./gradlew test
```

결과:

```text
BUILD SUCCESSFUL
```

확인된 범위:

- `product-api` 공개 계약 컴파일
- `product-core` 구현체 컴파일
- `loan-svc`에서 `PdMngr.getPd(Long)` 호출 컴파일
- 상품 등록/조회 컨트롤러 테스트
- 상품 등록 후 조회 acceptance test
- 전체 Gradle multi-module test task

## 6. 현재 구조의 의미

이번 수정 이후 상품 ID 흐름은 다음처럼 단순화된다.

```text
POST /v1/products
  → InMemoryProductRepository.save()
  → pdId: Long 생성

GET /v1/products/{productId}
  → productId: Long
  → ProductRepository.findById(Long)

POST /v1/loans
  → request.pdId: Long
  → PdMngr.getPd(Long)
  → LnArrCreateSpec.pdId: Long
  → Arr.pdId: Long
```

이제 상품, 대출, 약정 모듈이 동일한 상품 식별자 타입을 공유한다.

## 7. 남은 개선 과제

### 7.1 예외 응답 표준화

현재 존재하지 않는 상품 조회나 한도 초과 같은 도메인 예외가 HTTP 응답으로 어떻게 매핑되는지 명확하지 않다.

권장 방향:

- `NoSuchElementException` 직접 노출 제거
- `ProductNotFoundException`, `LoanLimitExceededException` 같은 도메인 예외 정의
- `@RestControllerAdvice`에서 HTTP status와 error body 표준화

예시:

```json
{
  "code": "PRODUCT_NOT_FOUND",
  "message": "상품을 찾을 수 없습니다.",
  "details": {
    "productId": 1
  }
}
```

### 7.2 금액/금리 검증 강화

현재 상품 생성 요청의 `interestRate`, `maxLoanAmt`는 `@NotNull`만 적용되어 있다.

권장 검증:

- `interestRate > 0`
- `maxLoanAmt > 0`
- `lnAmt > 0`
- `lnAmt <= maxLoanAmt`
- 금리 scale 정책 명확화
- 금액 scale 정책 명확화

금융 도메인에서는 `BigDecimal`을 쓰는 것만으로 충분하지 않다. scale, rounding, 비교 기준을 별도로 정의해야 한다.

### 7.3 in-memory repository의 한계 명시

현재 repository는 in-memory 구현이다. 학습/프로토타입 단계에서는 충분하지만, 다음 한계가 있다.

- 애플리케이션 재시작 시 데이터 유실
- 다중 인스턴스 환경에서 데이터 불일치
- sequence 초기화
- 검색 조건 확장 어려움
- 트랜잭션 없음

향후 DB 전환 시 `ProductRepository`, `LoanRepository` 인터페이스는 유지하고 구현체만 교체하는 방향이 적절하다.

### 7.4 모듈 의존성 재점검

현재 `loan-svc`가 `product-core`에 직접 의존한다.

프로토타입 단계에서는 빠르게 통합하기 좋은 선택이지만, 장기적으로는 다음 중 하나를 선택해야 한다.

- 모놀리식 모듈 구조 유지: `product-core` auto-configuration으로 내부 Bean 공유
- 서비스 분리 구조 지향: `loan-svc`는 `ProductClient` 같은 ACL을 통해 상품 서비스 호출

팀이 어떤 아키텍처 방향을 선택하는지에 따라 `loan-svc → product-core` 의존은 유지될 수도 있고 제거될 수도 있다.

### 7.5 테스트 레이어 정리

현재 테스트는 일부 단위 테스트와 acceptance test가 섞여 있다.

권장 구분:

- Controller slice test: request validation, status code, JSON shape
- Service unit test: 한도 검증, 상품 조회 실패, 약정 생성 파라미터
- Repository test: ID 발급, findById, findAll
- Acceptance test: 실제 API 플로우 중심

특히 대출 신청 흐름은 다음 테스트가 필요하다.

- 존재하는 상품으로 대출 신청 성공
- 존재하지 않는 상품으로 신청 시 실패
- 신청 금액이 상품 한도를 초과하면 실패
- 신청 금액이 0 이하이면 400
- 응답의 금리가 상품 금리와 동일한지 확인

## 8. 팀 공유용 결론

이번 수정의 핵심은 단순히 컴파일 에러를 제거한 것이 아니라, 모듈 간 공개 계약을 다시 정렬한 것이다.

상품 ID는 상품, 대출, 약정을 관통하는 식별자다. 이 값의 타입이 모듈마다 다르면 서비스 간 통합 지점마다 변환 코드가 생기고, 장기적으로는 어떤 값이 진짜 도메인 ID인지 불명확해진다.

따라서 이번 단계에서는 `Long`을 표준 타입으로 정하고, `product-api`부터 `product-core`, `product-svc`, `loan-svc`까지 같은 기준으로 맞췄다.

현재 상태는 전체 테스트가 통과하며, 다음 개발은 예외 응답 표준화와 대출 신청 테스트 보강부터 진행하는 것이 좋다.
