# 대출 멀티조회 코드 이해 가이드

## 1. 이 문서의 목적

이 문서는 대출 멀티조회 기능을 처음 보는 팀원이 코드의 흐름을 빠르게 이해할 수 있도록 작성한 코드 리딩 가이드다.

현재 `src/main` 기준으로는 대출 신청 API만 구현되어 있고, 멀티조회 기능은 아직 완성된 코드로 들어와 있지 않다. 따라서 이 문서는 두 가지를 구분해서 설명한다.

- 현재 구현된 대출 신청 코드가 어떤 구조인지
- 멀티조회 기능이 들어오면 어떤 책임이 어디에 배치되어야 하는지

핵심은 “여러 상품을 한 번에 조회하고, 그 조회 결과를 근거로 대출 신청을 제한한다”는 흐름을 코드 레벨에서 이해하는 것이다.

## 2. 현재 코드의 출발점

현재 대출 API의 진입점은 `loan-svc`에 있다.

```text
POST /v1/loans
  -> LoanController.apply()
  -> LoanCommandService.apply()
  -> PdMngr.getPd(pdId)
  -> LoanRepository.save(LnArrCreateSpec)
```

관련 파일:

```text
loan-svc
  src/main/java/com/bank/loan/controller/LoanController.java
  src/main/java/com/bank/loan/service/LoanCommandService.java
  src/main/java/com/bank/loan/service/dto/LoanApplyRequest.java
  src/main/java/com/bank/loan/service/dto/LoanApplyResponse.java

loan-core
  src/main/java/com/bank/loan/core/domain/model/LnArrCreateSpec.java
  src/main/java/com/bank/loan/core/domain/model/LnArrImpl.java
  src/main/java/com/bank/loan/core/domain/repository/LoanRepository.java
  src/main/java/com/bank/loan/core/repository/InMemoryLoanRepository.java

loan-api
  src/main/java/com/bank/loanapi/model/LnArr.java
```

현재 요청 DTO는 다음 값을 받는다.

```java
public class LoanApplyRequest {
    private Long custId;
    private Long pdId;
    private BigDecimal lnAmt;
}
```

현재 신청 서비스의 핵심 로직은 다음과 같다.

```java
Pd pd = pdMngr.getPd(request.getPdId());

if (request.getLnAmt().compareTo(pd.getMaxLoanAmt()) > 0) {
    throw new IllegalStateException("신청금액이 상품 한도를 초과합니다.");
}

LnArr lnArr = loanRepository.save(new LnArrCreateSpec(...));
```

즉, 현재 구조는 “신청 시점에 상품을 바로 조회하고, 상품 한도와 신청 금액을 비교한 뒤 약정을 생성하는 방식”이다.

## 3. 현재 구조의 한계

단일 신청 구조에서는 다음 질문에 답하기 어렵다.

- 고객이 사전에 어떤 상품들을 조회했는가?
- 신청하려는 상품이 실제 조회 결과에 포함되어 있었는가?
- 한도조회 결과가 언제 생성되었는가?
- 오래된 조회 결과로 신청하는 것을 막을 수 있는가?
- 상품별 조회 결과가 외부 심사/보증/신용평가 결과를 반영했는가?

현재 코드는 `pd.maxLoanAmt`만 보고 신청 가능 여부를 판단한다. 멀티조회 기능이 들어오면 “상품의 기본 한도”가 아니라 “고객별, 상품별 조회 결과 한도”를 기준으로 신청을 판단해야 한다.

## 4. 멀티조회가 추가되면 바뀌는 개념

멀티조회 기능에서는 대출 신청 전에 한 단계가 추가된다.

```text
1단계: 여러 상품 한도조회
POST /v1/loans/inquiry

2단계: 조회 결과를 근거로 대출 신청
POST /v1/loans
```

기존 신청 API는 `custId + pdId + lnAmt`만 있으면 바로 신청이 가능했다.

멀티조회 도입 후에는 신청 요청에 `inquiryId`가 들어와야 한다.

```json
{
  "inquiryId": 1,
  "pdId": 10,
  "lnAmt": 20000000
}
```

이때 `custId`는 신청 요청에서 직접 받기보다 조회 이력에서 가져오는 편이 더 안전하다. 클라이언트가 임의로 `custId`를 바꿔서 신청하는 것을 막을 수 있기 때문이다.

## 5. 멀티조회 도메인 모델

멀티조회에는 최소 두 개의 도메인 개념이 필요하다.

### 5.1 한도조회 이력

한 번의 조회 요청 자체를 나타낸다.

```text
LnInquiry
  - inquiryId
  - custId
  - inquiryDt
  - results
```

`inquiryId`는 이후 대출 신청에서 조회 결과를 다시 찾기 위한 키다.

### 5.2 상품별 한도조회 결과

조회한 상품 하나에 대한 결과다.

```text
LnInquiryResult
  - pdId
  - maxLoanAmt
  - intrRt
```

하나의 `LnInquiry`는 여러 개의 `LnInquiryResult`를 가진다.

```text
LnInquiry
  ├─ result(pdId=10, maxLoanAmt=50,000,000, intrRt=4.5%)
  ├─ result(pdId=20, maxLoanAmt=30,000,000, intrRt=3.9%)
  └─ result(pdId=30, maxLoanAmt=10,000,000, intrRt=6.1%)
```

## 6. 요청 흐름

### 6.1 한도조회 요청

```text
POST /v1/loans/inquiry
```

요청:

```json
{
  "custId": 1,
  "pdIds": [10, 20, 30]
}
```

처리 흐름:

```text
LoanController.inquiry()
  -> LoanCommandService.inquiry()
     -> 각 pdId별 상품/외부정보 조회
     -> 상품별 한도/금리 결과 생성
     -> LnInquiryRepository.save()
     -> LoanInquiryResponse 반환
```

응답:

```json
{
  "inquiryId": 1,
  "custId": 1,
  "inquiryDt": "2026-04-29",
  "results": [
    {
      "pdId": 10,
      "maxLoanAmt": 50000000,
      "intrRt": 0.045
    }
  ]
}
```

### 6.2 대출 신청 요청

```text
POST /v1/loans
```

요청:

```json
{
  "inquiryId": 1,
  "pdId": 10,
  "lnAmt": 20000000
}
```

처리 흐름:

```text
LoanController.apply()
  -> LoanCommandService.apply()
     -> LnInquiryRepository.findById(inquiryId)
     -> 조회 이력이 오늘 생성된 것인지 확인
     -> 신청 pdId가 조회 결과에 포함되어 있는지 확인
     -> 신청 금액이 조회 결과 한도 이하인지 확인
     -> LoanRepository.save(LnArrCreateSpec)
     -> LoanApplyResponse 반환
```

여기서 중요한 점은 신청 시점에 상품을 다시 단순 조회해서 한도를 판단하지 않는다는 것이다. 신청 판단의 근거는 반드시 `inquiryId`에 연결된 조회 결과여야 한다.

## 7. 코드 책임 분리

### 7.1 `loan-svc`

HTTP 요청/응답과 애플리케이션 유스케이스를 담당한다.

들어와야 할 클래스:

```text
LoanInquiryRequest
LoanInquiryResponse
LoanInquiryResultItem
LoanApplyRequest
LoanCommandService
LoanController
```

`LoanCommandService`는 멀티조회의 핵심 오케스트레이션을 담당한다.

```text
inquiry()
  - 요청 검증
  - 상품별 조회 전략 실행
  - 조회 이력 저장
  - 응답 DTO 변환

apply()
  - inquiryId 조회
  - 조회 결과 유효성 검증
  - 대출 약정 생성
```

### 7.2 `loan-core`

대출 도메인의 상태와 저장소 계약을 담당한다.

들어와야 할 클래스:

```text
LnInquiry
LnInquiryResult
LnInquiryImpl
LnInquiryResultImpl
LnInquiryCreateSpec
LnInquiryRepository
InMemoryLnInquiryRepository
```

`loan-core`는 HTTP나 JSON을 알면 안 된다. 순수하게 조회 이력과 대출 약정이라는 도메인 개념만 다루는 편이 좋다.

### 7.3 `loan-api`

다른 모듈이 참조할 수 있는 공개 계약을 둔다.

들어와야 할 인터페이스:

```text
LnInquiry
LnInquiryResult
```

현재 `LnArr`가 `loan-api`에 있는 것처럼, 조회 이력도 외부에서 읽을 필요가 있다면 `loan-api`에 계약을 두는 것이 자연스럽다.

### 7.4 상품/외부기관 연동

멀티조회는 단순히 상품 테이블만 읽는 기능이 아니다. 실제 대출 도메인에서는 상품, 신용평가, 보증 가능 여부, 고객 상태 등의 결과가 합쳐져야 한다.

따라서 `loan-svc` 내부에 ACL 성격의 client를 두는 것이 좋다.

```text
ProductClient
NiceCreditClient
KcbCreditClient
KifaClient
SgiClient
```

초기에는 in-memory 구현을 사용하더라도, 서비스 코드가 특정 구현체에 직접 묶이지 않도록 인터페이스를 먼저 두는 편이 좋다.

## 8. 신청 검증 규칙

멀티조회 기반 신청에서는 다음 순서로 검증해야 한다.

```text
1. inquiryId가 존재하는가?
2. 조회 일자가 오늘인가?
3. 신청 pdId가 조회 결과에 포함되어 있는가?
4. 신청 금액이 해당 상품의 조회 한도 이하인가?
5. 검증 통과 후 약정을 생성한다.
```

이 순서를 지키는 이유는 에러 원인을 명확히 하기 위해서다.

예를 들어 `inquiryId`가 존재하지 않는데 먼저 한도 비교를 하려고 하면, 어떤 예외를 내야 하는지 불명확해진다. 조회 이력 확인이 가장 먼저다.

## 9. 현재 단일 신청 코드에서 멀티조회 코드로 바뀌는 지점

현재:

```java
Pd pd = pdMngr.getPd(request.getPdId());

if (request.getLnAmt().compareTo(pd.getMaxLoanAmt()) > 0) {
    throw new IllegalStateException(...);
}
```

멀티조회 이후:

```java
LnInquiry inquiry = inquiryRepository.findById(request.getInquiryId())
        .orElseThrow(...);

LnInquiryResult result = inquiry.getResults().stream()
        .filter(it -> it.getPdId().equals(request.getPdId()))
        .findFirst()
        .orElseThrow(...);

if (request.getLnAmt().compareTo(result.getMaxLoanAmt()) > 0) {
    throw new IllegalStateException(...);
}
```

차이는 판단 근거다.

현재는 `Pd.maxLoanAmt`를 본다.  
멀티조회 이후에는 `LnInquiryResult.maxLoanAmt`를 본다.

이 차이가 중요하다. 상품의 기본 한도와 고객별 조회 한도는 같은 값이 아닐 수 있다.

## 10. 전략 패턴을 쓸 수 있는 지점

상품마다 한도 산출 방식이 다르면 `LoanCommandService` 안에 if문을 계속 늘리면 안 된다.

예를 들어 다음처럼 상품 유형별 전략을 둘 수 있다.

```text
LnInquiryStrategy
  ├─ CreditLoanInquiryStrategy
  ├─ SaitdolInquiryStrategy
  └─ HaetsallonInquiryStrategy
```

각 전략의 책임:

```text
supports(pdId or productType)
inquire(custId, product)
```

서비스는 전략 목록을 주입받아 적절한 전략을 선택한다.

```java
LnInquiryStrategy strategy = strategies.stream()
        .filter(it -> it.supports(product))
        .findFirst()
        .orElseThrow(...);
```

이 구조의 장점:

- 상품별 심사 로직이 분리된다.
- 신규 상품 추가 시 기존 서비스 코드를 덜 건드린다.
- 전략별 단위 테스트가 쉬워진다.
- 외부기관 client 호출 조합을 상품별로 다르게 가져갈 수 있다.

## 11. 읽는 순서

멀티조회 코드를 구현하거나 리뷰할 때는 다음 순서로 읽는 것이 좋다.

```text
1. LoanController
   - API path가 무엇인지
   - request/response DTO가 무엇인지

2. LoanCommandService
   - 유스케이스 흐름이 어디까지 한 메서드에 들어가 있는지
   - 조회와 신청이 분리되어 있는지

3. DTO
   - request가 클라이언트에게 어떤 책임을 요구하는지
   - response가 외부에 어떤 계약을 노출하는지

4. loan-core model
   - 조회 이력과 결과가 불변 객체인지
   - 약정 생성에 필요한 값이 명확히 분리되어 있는지

5. repository
   - ID 발급 주체가 어디인지
   - 조회 결과 저장/조회 기준이 무엇인지

6. strategy/client
   - 상품별 한도 산출 로직이 서비스에 섞이지 않았는지
   - 외부 연동이 도메인 모델을 오염시키지 않는지
```

## 12. 리뷰할 때 봐야 할 포인트

멀티조회 PR을 리뷰할 때는 아래 항목을 중점적으로 보면 된다.

- `LoanApplyRequest`에서 `custId`가 제거되고 `inquiryId`가 들어왔는가?
- 신청 시 `Pd.maxLoanAmt`가 아니라 `LnInquiryResult.maxLoanAmt`를 기준으로 검증하는가?
- 조회 이력의 유효기간을 검증하는가?
- 조회하지 않은 상품으로 신청할 수 없게 막는가?
- 조회 결과 저장소와 약정 저장소가 분리되어 있는가?
- 상품별 한도 산출 로직이 `LoanCommandService`에 과도하게 몰려 있지 않은가?
- 외부기관 client가 도메인 모델에 직접 침투하지 않는가?
- 실패 케이스 테스트가 충분한가?

## 13. 필요한 테스트

최소한 다음 테스트는 있어야 한다.

### 한도조회

- 복수 상품을 조회하면 상품별 결과가 반환된다.
- 빈 `pdIds` 요청은 400을 반환한다.
- 존재하지 않는 상품 ID가 들어왔을 때 정책대로 처리된다.
- 상품별 전략이 올바르게 선택된다.

### 대출신청

- 당일 조회 결과로 신청하면 약정이 생성된다.
- 존재하지 않는 `inquiryId`로 신청하면 실패한다.
- 만료된 조회 이력으로 신청하면 실패한다.
- 조회하지 않은 `pdId`로 신청하면 실패한다.
- 신청 금액이 조회 한도를 초과하면 실패한다.
- 신청 금액이 조회 한도와 같으면 성공한다.

## 14. 팀 내 합의가 필요한 지점

아래는 구현 전에 팀에서 결정해야 하는 정책이다.

### 14.1 조회 결과 유효기간

현재 스펙은 “당일만 유효”로 보는 것이 자연스럽다.

```java
inquiry.getInquiryDt().equals(LocalDate.now())
```

다만 운영 환경에서는 시간대, 영업일, 심사 결과 재사용 정책이 들어갈 수 있다. 지금은 `Clock` 주입을 고려해 테스트 가능성을 확보하는 편이 좋다.

### 14.2 일부 상품 조회 실패 처리

복수 상품 조회 중 일부 상품만 실패했을 때 정책이 필요하다.

선택지:

- 전체 실패
- 실패 상품 제외 후 성공 상품만 반환
- 상품별 실패 사유를 결과에 포함

금융 서비스 관점에서는 상품별 실패 사유를 명시하는 방식이 가장 투명하지만, 초기 구현은 성공 상품만 반환하거나 전체 실패로 단순화할 수 있다.

### 14.3 외부기관 호출 실패 처리

신용평가나 보증기관 호출 실패 시 재시도, timeout, fallback 정책이 필요하다.

초기 in-memory 구현에서는 드러나지 않지만, 인터페이스 설계 시 checked/unchecked 예외와 실패 응답 모델을 미리 정해두는 것이 좋다.

## 15. 결론

멀티조회 기능의 핵심은 API를 하나 추가하는 것이 아니다.

핵심은 대출 신청의 판단 근거를 “상품 기본 정보”에서 “고객이 특정 시점에 조회한 상품별 한도 결과”로 바꾸는 것이다.

따라서 구현의 중심은 다음 세 가지다.

- 조회 이력을 저장하는 모델과 repository
- 상품별 한도 산출을 분리하는 strategy/client 구조
- 신청 시 조회 이력을 검증하고 그 결과를 기준으로 약정을 생성하는 application service

이 구조가 잡히면 상품이 늘어나거나 외부기관 연동이 추가되어도 `LoanCommandService`가 비대해지는 것을 막을 수 있다.
