# loan-application
금융 대출 서비스 (상품관리, 한도조회, 대출실행, 상환관리)

## 객체 관계도

```
┌─────────────────────────────────────────────────────────────────────┐
│  loan-core                                                          │
│                                                                     │
│  Arr / LnArr / LnInquiry / LnInquiryResult                          │
│  ArrImpl / LnArrImpl / LnInquiryImpl / LnInquiryResultImpl          │
│  ArrSttsEnum / ArrTpEnum                                            │
│                                                                     │
│  LnArrImpl  extends ArrImpl, implements LnArr                       │
│  ──────────────────────────────────────                             │
│  - lnAmt: BigDecimal                                                │
│  - intrRt: BigDecimal                                               │
│                                                                     │
│  LnArrCreateSpec            LoanRepository <<interface>>            │
│  ───────────────            ─────────────────────────────           │
│  - custId: Long             + save(LnArrCreateSpec): LnArr          │
│  - pdId: Long               + findById(Long): Optional<LnArr>       │
│  - lnAmt: BigDecimal        + findAll(): List<LnArr>                │
│  - intrRt: BigDecimal                                               │
│  - arrStrtDt: LocalDate     InMemoryLoanRepository                  │
│  - arrEndDt: LocalDate        implements LoanRepository             │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│  product-api                                                        │
│                                                                     │
│  <<interface>>              <<interface>>                           │
│  Pd                         PdMngr                                  │
│  ─────────────────          ──────────────────                      │
│  + getPdId(): Long          + getPd(Long): Pd                       │
│  + getPdNm(): String                                                │
│  + getInterestRate(): BigDecimal                                    │
│  + getMaxLoanAmt(): BigDecimal                                      │
└─────────────────┬───────────────────────────────┬───────────────────┘
                  │ implements                     │ implements
┌─────────────────▼───────────────────────────────▼───────────────────┐
│  product-core                                                       │
│                                                                     │
│  PdImpl                     PdMngrImpl           ProductRepository  │
│  implements Pd              implements PdMngr    <<interface>>       │
│  ─────────────────          ──────────────────   ────────────────── │
│  - pdId: Long               - repository         + save(...)        │
│  - pdNm: String                                  + findById(Long)   │
│  - interestRate: BigDecimal  InMemoryProductRepository              │
│  - maxLoanAmt: BigDecimal      implements ProductRepository         │
│                                                                     │
│  ProductAutoConfiguration  (@AutoConfiguration)                     │
│  → PdMngr, ProductRepository 빈 자동 등록                            │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│  loan-svc  (POST /v1/loans)                                         │
│                                                                     │
│  LoanController                                                     │
│       │ @Valid LoanApplyRequest                                     │
│       ▼                                                             │
│  LoanApplicationService / LoanExecutionService                      │
│       │ 1. POST /v1/loans/inquiry  → 상품별 한도조회                 │
│       │ 2. POST /v1/loans          → 조회 이력 기반 대출 실행        │
│       │ 3. LoanRepository.save(spec) → 대출 계약 생성                │
│       ▼                                                             │
│  LoanApplyResponse (arrId, custId, pdId, lnAmt, intrRt, ...)       │
└─────────────────────────────────────────────────────────────────────┘
```

## 모듈 구조

| 모듈 | 역할 |
|------|------|
| `loan-core` | 대출 도메인 계약, 모델, 저장소 구현 |
| `loan-svc` | 한도조회/대출실행 API 서비스 (포트 9000) |
| `product-api` | 상품 인터페이스 (`Pd`, `PdMngr`) |
| `product-core` | 상품 도메인 모델, 저장소, 자동 구성 |
| `product-svc` | 상품 등록/조회 API 서비스 (포트 9001) |
