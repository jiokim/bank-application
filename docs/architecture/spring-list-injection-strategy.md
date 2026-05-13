# Spring List 주입을 활용한 Multi-Strategy 패턴

## 개요

상품별로 다른 한도조회 로직을 수행해야 할 때, Spring의 `List<T>` Bean 자동 수집 기능과 Strategy 패턴을 결합하여 개방-폐쇄 원칙을 지킨다.

## 구조

```
LnInquiryStrategy (interface)
├── supportedPdId(): Long       ← 이 전략이 담당하는 상품 ID
└── inquire(...): CompletableFuture<LnInquiryResult>

CreditLoanInquiryStrategy  @Component  pdId=1  (신용대출 - NICE/KCB 신용점수 기반)
SaitdolInquiryStrategy     @Component  pdId=2  (사잇돌 - SGI 보증 기반)
HaetsallonInquiryStrategy  @Component  pdId=3  (햇살론 - KIFA 보증 기반)
```

## 동작 방식

```java
// LoanApplicationService
private final List<LnInquiryStrategy> strategies;  // Spring이 @Component 전부 수집

public LoanInquiryInfo inquiry(LoanInquiryCommand command) {
    // 매 요청마다 pdId → strategy 맵으로 변환
    Map<Long, LnInquiryStrategy> strategyMap = strategies.stream()
            .collect(Collectors.toMap(LnInquiryStrategy::supportedPdId, Function.identity()));

    // 요청된 상품들을 병렬로 한도조회
    List<CompletableFuture<LnInquiryResult>> futures = command.getPdIds().stream()
            .map(pdId -> strategyMap.get(pdId).inquire(command.getCustId(), nice, kcb))
            .toList();

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    ...
}
```

## 핵심 특징

- **Bean 등록만으로 확장**: 새 상품 추가 시 `LnInquiryStrategy` 구현체에 `@Component`만 붙이면 된다. `LoanApplicationService` 수정 불필요.
- **각 전략은 자기 외부 클라이언트를 직접 주입받음**: `HaetsallonInquiryStrategy`는 `KifaClient`, `SaitdolInquiryStrategy`는 `SgiClient`를 각자 가짐.
- **한도조회는 전략 간 병렬 실행**: `CompletableFuture` 리스트로 동시 호출 후 `allOf`로 집계.
- **discriminator는 `supportedPdId()`**: pdId를 키로 Map 변환하여 O(1) 라우팅.

## 주의

- `strategyMap`을 매 요청마다 생성하고 있음 — 빈도가 높다면 생성자에서 한 번만 만들어 캐시하는 게 낫다.
- 미지원 pdId 요청 시 `NoSuchElementException` 던짐 — 호출 전 검증 레이어 추가 고려.

## 관련 파일

| 파일 | 역할 |
|------|------|
| `loan-svc/.../multistrategy/LnInquiryStrategy.java` | 전략 인터페이스 |
| `loan-svc/.../multistrategy/CreditLoanInquiryStrategy.java` | pdId=1 신용대출 |
| `loan-svc/.../multistrategy/SaitdolInquiryStrategy.java` | pdId=2 사잇돌 |
| `loan-svc/.../multistrategy/HaetsallonInquiryStrategy.java` | pdId=3 햇살론 |
| `loan-svc/.../application/LoanApplicationService.java` | 전략 수집 및 라우팅 |