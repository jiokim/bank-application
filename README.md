# loan-application
금융 대출 서비스 (상품관리, 한도조회, 대출실행, 상환관리)

## 객체 관계도

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ arrangement-core                                                             │
│                                                                              │
│  <<interface>> Arr                         <<enum>> ArrTpEnum                │
│  ─────────────────────────                 ─────────────────                 │
│  + getArrId(): Long                        LN("01"), DP("02")                │
│  + getArrTpCd(): ArrTpEnum                                                    │
│  + getCustId(): Long                       <<enum>> ArrSttsEnum              │
│  + getArrSttsCd(): ArrSttsEnum             ───────────────────               │
│  + getArrStrtDt(): LocalDate               ACTIVE("A"), TERMINATE("T")       │
│  + getArrEndDt(): LocalDate                                                   │
│  + getPdId(): Long                                                            │
│                                                                              │
│  <<abstract>> ArrImpl implements Arr                                          │
│  - arrId, arrTpCd, custId, arrSttsCd, arrStrtDt, arrEndDt, pdId              │
└───────────────────────────────┬──────────────────────────────────────────────┘
                                │ extends
          ┌─────────────────────┴─────────────────────┐
          │                                           │
┌─────────▼────────────────────────────────┐ ┌────────▼───────────────────────────────┐
│ loan-core                                │ │ deposit-core                            │
│                                          │ │                                        │
│ <<interface>> LnArr extends Arr          │ │ <<interface>> DpArr extends Arr        │
│ + getLnAmt(): BigDecimal                 │ │ + getDpAmt(): BigDecimal               │
│ + getIntrRt(): BigDecimal                │ │ + getIntrRt(): BigDecimal              │
│                                          │ │ + getMaturityDt(): LocalDate           │
│ LnArrImpl extends ArrImpl                │ │ DpArrImpl extends ArrImpl              │
│                                          │ │                                        │
│ LoanRepository                           │ │ DepositRepository                      │
│ InMemoryLoanRepository                   │ │ InMemoryDepositRepository              │
│                                          │ │                                        │
│ LnInquiry / LnInquiryResult              │ │                                        │
│ LnInquiryRepository                      │ │                                        │
└──────────────────────────────────────────┘ └────────────────────────────────────────┘
```

## 모듈 관계도

```
                                      ┌────────────────────┐
                                      │  arrangement-core   │
                                      │  공통 약정 모델      │
                                      └─────────▲──────────┘
                                                │
                           ┌────────────────────┴────────────────────┐
                           │                                         │
                  ┌────────┴────────┐                       ┌────────┴────────┐
                  │    loan-core    │                       │  deposit-core   │
                  │ 대출 약정/조회   │                       │ 예금 약정       │
                  └───────▲─┬───────┘                       └────────┬────────┘
                          │ │                                        │
                          │ │ uses                                   │ uses
                          │ ▼                                        ▼
                  ┌───────┴──────────────────────────────────────────┴────────┐
                  │                    product-api                            │
                  │                    Pd / PdMngr 계약                        │
                  └──────────────────────────▲────────────────────────────────┘
                                             │ implements
                                             │
                                      ┌──────┴───────┐
                                      │ product-core │
                                      │ 상품 도메인/저장소 │
                                      └──────────────┘

┌────────────────────┐
│      loan-svc      │
│ 한도조회/대출실행 API │
└─────────┬──────────┘
          │
          │ uses loan-core and product-core implementation
          ▼
┌────────────────────┐
│ loan-core/product-core │
└────────────────────┘

┌────────────────────┐
│    product-svc     │
│ 상품 등록/조회 API  │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ product-core/api   │
└────────────────────┘
```

## 모듈 구조

| 모듈 | 역할 |
|------|------|
| `arrangement-core` | 모든 약정이 공유하는 공통 계약 (`Arr`, `ArrImpl`, `ArrTpEnum`, `ArrSttsEnum`) |
| `loan-core` | 대출 도메인 계약, 모델, 저장소 구현. `product-api`의 상품 계약을 사용 |
| `deposit-core` | 예금 도메인 계약, 모델, 저장소 구현. `product-api`의 상품 계약을 사용 |
| `loan-svc` | 한도조회/대출실행 API 서비스 (포트 9000) |
| `product-api` | 상품 인터페이스 (`Pd`, `PdMngr`) |
| `product-core` | 상품 도메인 모델, 저장소, 자동 구성 |
| `product-svc` | 상품 등록/조회 API 서비스 (포트 9001) |

## 테이블 매핑 관점

현재 코드는 in-memory 저장소 기반이지만, 객체 구조는 다음 테이블 분리 구조를 전제로 한다.

```
arrangement
  ├─ loan_arrangement
  └─ deposit_arrangement
```

| 객체 | 테이블 | 설명 |
|------|--------|------|
| `Arr`, `ArrImpl` | `arrangement` | 약정 ID, 고객 ID, 상품 ID, 상태, 기간 등 공통 필드 |
| `LnArr`, `LnArrImpl` | `loan_arrangement` | 대출 금액, 대출 금리 등 대출 전용 필드 |
| `DpArr`, `DpArrImpl` | `deposit_arrangement` | 예치 금액, 예금 금리, 만기일 등 예금 전용 필드 |
