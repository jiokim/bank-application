# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build                         # Build all modules
./gradlew clean                         # Clean build artifacts
./gradlew loan-svc:bootRun              # Run loan service (port 9000)
./gradlew product-svc:bootRun           # Run product service (port 9001)
./gradlew loan-svc:bootJar              # Build executable JAR for loan
./gradlew product-svc:bootJar          # Build executable JAR for product
```

## Testing

```bash
./gradlew test                                                          # Run all tests
./gradlew loan-svc:test                                                 # Run loan module tests only
./gradlew product-svc:test                                              # Run product module tests only
./gradlew test --tests "com.bank.product.SomeTest"                      # Run a single test class
./gradlew test --tests "com.bank.product.SomeTest.methodName"           # Run a single test method
```

## Architecture

**Spring Boot 4.0.5 multi-module Gradle project.** Java 21, Spring MVC, Lombok, JUnit 5 + Rest-Assured.

Swagger UI: `http://localhost:9000/swagger-ui.html` (loan-svc)

### Module Layout

Convention plugins live in `build-logic/src/main/groovy/`:
- `bank.java-common` — Java 21 toolchain + Lombok
- `bank.spring-library` — `bank.java-common` + `java-library` + Spring dependency-management; disables `bootJar`, enables `jar`
- `bank.spring-app` — `bank.java-common` + Spring Boot + web/validation/test dependencies

| Module | Plugin | Role |
|--------|--------|------|
| `bank-common` | `bank.java-common` | Pure Java cross-cutting contracts: `BankRequestContext`, `@Sensitive`, `SensitiveFieldProcessor`, `TextEncryptor` |
| `bank-common-core` | `bank.spring-library` | Spring auto-configuration for `bank-common`: interceptor, async executor, `PreServiceSensitiveFieldHandler` |
| `arrangement-core` | `bank.spring-library` | Common arrangement model: `Arr` interface, `ArrImpl` (abstract), `ArrTpEnum`, `ArrSttsEnum` |
| `loan-core` | `bank.spring-library` | Loan domain: `LnArr`/`LnArrImpl`, `LnInquiry`/`LnInquiryResult`, `LoanRepository`, `LnInquiryRepository` |
| `deposit-core` | `bank.spring-library` | Deposit domain: `DpArr`/`DpArrImpl`, `DepositRepository` |
| `product-api` | `bank.java-common` | Pure Java product interfaces: `Pd`, `LnPd`, `DpPd`, `PdMngr<T>` |
| `product-core` | `bank.spring-library` | Product implementations: `LnPdMngrImpl`, `DpPdMngrImpl` + `ProductAutoConfiguration` |
| `loan-svc` | `bank.spring-app` | Spring Boot app (port 9000): loan limit inquiry and execution APIs |
| `product-svc` | `bank.spring-app` | Spring Boot app (port 9001): product registration and query APIs |

`-core` modules register beans via `@AutoConfiguration` + `@ConditionalOnMissingBean`, allowing `-svc` modules to override them.

### Domain Model Hierarchy

```
arrangement-core →  Arr (interface: arrId, custId, pdId, arrTpCd, arrSttsCd, dates)
                    ArrImpl (abstract, implements Arr)
                        │
          ┌─────────────┴─────────────┐
          │                           │
loan-core → LnArr extends Arr      deposit-core → DpArr extends Arr
            LnArrImpl extends ArrImpl              DpArrImpl extends ArrImpl
            LnInquiry / LnInquiryResult

product-api → Pd (pdNm, interestRate)
                └─ LnPd (maxLoanAmt)
                └─ DpPd
              PdMngr<T extends Pd>

product-core → LnPdMngrImpl, DpPdMngrImpl implements PdMngr
```

### Cross-Cutting Framework (`bank-common` / `bank-common-core`)

**Request context** — `BankRequestContext` (staffId, channelCode, txDate) is bound per-request via `BankRequestContextInterceptor` (reads `X-Staff-Id`, `X-Channel-Code` headers) and stored in `BankRequestContextHolder` (ThreadLocal). `BankRequestContextTaskDecorator` propagates the context into async threads managed by `bankAsyncExecutor`.

**Sensitive field encryption** — `@Sensitive(storagePolicy = StoragePolicy.ENCRYPT)` on a mutable `String` field. `SensitiveFieldProcessor` walks the object graph via reflection and encrypts/decrypts. `PreServiceSensitiveFieldHandler` (`RequestBodyAdvice`) automatically encrypts all `@Sensitive` fields on deserialized request bodies before any service logic runs.

Encryption key is configured via `bank.common.sensitive.encryption-key` (defaults to a dev key if absent).

### Loan Inquiry Strategy Pattern

`LoanApplicationService` fetches credit scores from NICE and KCB in parallel, then dispatches each requested product to a `LnInquiryStrategy` implementation (also in parallel via `CompletableFuture`). Each strategy maps to one product ID (`supportedPdId()`) and encapsulates that product's limit calculation logic.

External client interfaces and their in-memory stubs live in `loan-svc/src/main/java/com/bank/loan/client/`:
- `ProductClient` / `InMemoryProductClient`
- `NiceCreditClient` / `InMemoryNiceCreditClient` — NICE credit bureau
- `KcbCreditClient` / `InMemoryKcbCreditClient` — KCB credit bureau
- `KifaClient` / `InMemoryKifaClient` — KIFA guarantee agency
- `SgiClient` / `InMemorySgiClient` — SGI guarantee agency

Replace `InMemory*` implementations with HTTP clients when integrating with real external systems.

### Naming Conventions

Korean banking abbreviations are used throughout:

| Abbrev | Meaning |
|--------|---------|
| `Ln`   | 대출 (Loan) |
| `Dp`   | 예금 (Deposit) |
| `Arr`  | 약정/계약 (Arrangement) |
| `Pd`   | 상품 (Product) |
| `Mngr` | Manager |
| `Stts` | Status |
| `Tp`   | Type |
| `Rt`   | Rate |
| `Amt`  | Amount |
| `Cust` | Customer |
| `Intr` | Interest |
| `Strt/End` | Start/End |

### 패키지 규칙

| 패키지 | 용도 | 규칙 |
|--------|------|------|
| `client/<vendor>/` | 외부 시스템 HTTP 클라이언트 전용 | `*Client` 인터페이스 + DTO + `InMemory*` 스텁만. 내부 도메인 검증 로직 금지. |
| `service/application/eligibility/` | 신청가능여부확인 (신청 1단계) | `application/` 하위에 위치. 형제 패키지로 분리 금지. |
| `service/application/multistrategy/` | 한도조회 전략 (신청 2단계) | |
| `service/execution/` | 대출 실행 (신청 3단계) | |
| `service/query/` | 조회 전용 | |
| `controller/dto/` | HTTP 요청/응답 DTO | `Loan` full prefix 허용 (API 가독성) |

**핵심 규칙:** 내부 DB를 조회하는 클래스는 `service/`에. `client/`는 외부 시스템 경계 전용.