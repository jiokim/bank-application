# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build                         # Build all modules
./gradlew clean                         # Clean build artifacts
./gradlew loan-svc:bootRun              # Run loan service (port 9000)
./gradlew product-svc:bootRun           # Run product service (port 9001)
./gradlew arrangement-svc:bootRun       # Run arrangement service (port 9002)
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

### Module Layout

Each domain (`arrangement`, `loan`, `product`) is split into three modules:

| Suffix  | Convention Plugin      | Role |
|---------|------------------------|------|
| `-api`  | `bank.java-common`     | Pure Java interfaces — shared contracts with no Spring dependency |
| `-core` | `bank.spring-library`  | Domain implementations + Spring `@AutoConfiguration`; published as `java-library` |
| `-svc`  | `bank.spring-app`      | Spring Boot executable; depends on its own `-core` |

Convention plugins live in `build-logic/src/main/groovy/`:
- `bank.java-common` — Java 21 toolchain + Lombok
- `bank.spring-library` — `bank.java-common` + `java-library` + Spring dependency-management; disables `bootJar`, enables `jar`
- `bank.spring-app` — `bank.java-common` + Spring Boot + web/validation/test dependencies

### Domain Model Hierarchy

```
arrangement-api  →  Arr (interface: arrId, custId, pdId, arrTpCd, arrSttsCd, dates)
  loan-api       →  LnArr extends Arr  (+lnAmt, intrRt)
  product-api    →  Pd (+pdNm, interestRate, maxLoanAmt), PdMngr

arrangement-core →  ArrImpl (abstract, implements Arr)
  loan-core      →  LnArrImpl extends ArrImpl implements LnArr
  product-core   →  PdImpl, PdMngrImpl + ProductAutoConfiguration
```

`-core` modules register beans via Spring Boot auto-configuration (`@AutoConfiguration` + `@ConditionalOnMissingBean`), allowing `-svc` modules to override them.

### Cross-Service Communication

`loan-svc` calls product data through a `ProductClient` interface (`loan-svc/src/main/java/com/bank/loan/client/`). The current implementation is `InMemoryProductClient`. Replace with an HTTP client when integrating with `product-svc`.

### Naming Conventions

Korean banking abbreviations are used throughout:

| Abbrev | Meaning |
|--------|---------|
| `Ln`   | 대출 (Loan) |
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