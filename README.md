# loan-application
금융 대출 서비스 (상품관리, 한도조회, 대출실행, 상환관리)

## 객체 관계도

```
┌─────────────────────────────────────────────────────────────────────┐
│  arrangement-api                                                    │
│                                                                     │
│  <<interface>>              <<enum>>          <<enum>>              │
│  Arr                        ArrTpEnum         ArrSttsEnum           │
│  ─────────────────          ─────────         ───────────           │
│  + getArrId(): Long         LN ("01")         ACTIVE ("A")          │
│  + getArrTpCd(): ArrTpEnum  DP ("02")         TERMINATE ("T")       │
│  + getCustId(): Long                                                │
│  + getArrSttsCd(): ArrSttsEnum                                      │
│  + getArrStrtDt(): LocalDate                                        │
│  + getArrEndDt(): LocalDate                                         │
│  + getPdId(): Long                                                  │
└────────────────┬────────────────────────────────────────────────────┘
                 │ implements
┌────────────────▼────────────────────────────────────────────────────┐
│  arrangement-core                                                   │
│                                                                     │
│  <<abstract>>                                                       │
│  ArrImpl  implements Arr                                            │
│  ────────────────────────────────                                   │
│  - arrId: Long                                                      │
│  - arrTpCd: ArrTpEnum                                               │
│  - custId: Long                                                     │
│  - arrSttsCd: ArrSttsEnum                                           │
│  - arrStrtDt: LocalDate                                             │
│  - arrEndDt: LocalDate                                              │
│  - pdId: Long                                                       │
└────────────────┬────────────────────────────────────────────────────┘
                 │ extends
┌────────────────┼────────────────────────────────────────────────────┐
│  loan-api      │                                                    │
│                │  <<interface>>                                     │
│                │  LnArr  extends Arr                                │
│                │  ────────────────                                  │
│                │  + getLnAmt(): BigDecimal                          │
│                │  + getIntrRt(): BigDecimal                         │
│                │                                                    │
└────────────────┼───────────────┬────────────────────────────────────┘
                 │ extends       │ implements
┌────────────────▼───────────────▼────────────────────────────────────┐
│  loan-core                                                          │
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
│  LoanCommandService                                                 │
│       │ 1. PdMngr.getPd(pdId)          → 상품 조회                   │
│       │ 2. lnAmt ≤ pd.maxLoanAmt 검증  → 한도 초과 시 예외           │
│       │ 3. LoanRepository.save(spec)   → 대출 계약 생성              │
│       ▼                                                             │
│  LoanApplyResponse (arrId, custId, pdId, lnAmt, intrRt, ...)       │
└─────────────────────────────────────────────────────────────────────┘
```

## 모듈 구조

| 모듈 | 역할 |
|------|------|
| `arrangement-api` | 계약 공통 인터페이스 (`Arr`) 및 열거형 정의 |
| `arrangement-core` | `ArrImpl` 추상 구현체 |
| `arrangement-svc` | 계약 서비스 (포트 9002, 뼈대) |
| `loan-api` | 대출 계약 인터페이스 (`LnArr`) |
| `loan-core` | 대출 도메인 모델 및 저장소 구현 |
| `loan-svc` | 대출 신청 API 서비스 (포트 9000) |
| `product-api` | 상품 인터페이스 (`Pd`, `PdMngr`) |
| `product-core` | 상품 도메인 모델, 저장소, 자동 구성 |
| `product-svc` | 상품 등록/조회 API 서비스 (포트 9001) |
