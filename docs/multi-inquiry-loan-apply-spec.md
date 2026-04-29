# 멀티 한도조회 대출신청 — 스펙 및 시나리오

## 1. 개요

고객이 여러 상품의 대출 한도를 한 번에 조회한 후, 원하는 상품을 선택하여 대출을 신청하는 2단계 플로우.

```
[1단계] POST /v1/loans/inquiry   →  inquiryId 발급 + 상품별 한도/금리 결과 반환
[2단계] POST /v1/loans           →  inquiryId + pdId + lnAmt 로 대출 약정 생성
```

한도조회 이력은 **당일(inquiryDt == 오늘)** 에만 유효하다.  
유효기간이 지나면 신용조회를 다시 해야 하므로 재조회가 필요하다.

---

## 2. API 스펙

### 2-1. 한도조회 `POST /v1/loans/inquiry`

**Request**

```json
{
  "custId": 1,
  "pdIds": [
    10,
    20,
    30
  ]
}
```

| 필드       | 타입             | 필수 | 설명                   |
|----------|----------------|----|----------------------|
| `custId` | String         | Y  | 고객 ID                |
| `pdIds`  | List\<String\> | Y  | 조회할 상품 ID 목록 (1개 이상) |

**Response `200 OK`**

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
    },
    {
      "pdId": 20,
      "maxLoanAmt": 30000000,
      "intrRt": 0.039
    }
  ]
}
```

| 필드                     | 타입         | 설명              |
|------------------------|------------|-----------------|
| `inquiryId`            | Long       | 조회 이력 ID        |
| `custId`               | Long       | 고객 ID           |
| `inquiryDt`            | LocalDate  | 조회 일자 (= 오늘)    |
| `results`              | List       | 상품별 한도/금리 결과    |
| `results[].pdId`       | Long       | 상품 ID           |
| `results[].maxLoanAmt` | BigDecimal | 해당 상품의 최대 대출 한도 |
| `results[].intrRt`     | BigDecimal | 해당 상품의 적용 금리    |

---

### 2-2. 대출신청 `POST /v1/loans`

**Request**

```json
{
  "inquiryId": 1,
  "pdId": 10,
  "lnAmt": 20000000
}
```

| 필드          | 타입         | 필수 | 설명         |
|-------------|------------|----|------------|
| `inquiryId` | Long       | Y  | 한도조회 이력 ID |
| `pdId`      | Long       | Y  | 선택한 상품 ID  |
| `lnAmt`     | BigDecimal | Y  | 신청 금액 (양수) |

**Response `200 OK`**

```json
{
  "arrId": 1,
  "custId": 1,
  "pdId": 10,
  "lnAmt": 20000000,
  "intrRt": 0.045,
  "arrSttsCd": "ACTIVE",
  "arrStrtDt": "2026-04-29",
  "arrEndDt": "2027-04-29"
}
```

---

## 3. 검증 규칙

### 한도조회

| 규칙                 | 처리                            |
|--------------------|-------------------------------|
| `custId` 누락        | 400 Bad Request               |
| `pdIds` 누락 또는 빈 목록 | 400 Bad Request               |
| 존재하지 않는 `pdId`     | 해당 상품 결과에서 제외 (또는 예외 — 추후 결정) |

### 대출신청

| 규칙                      | 처리              |
|-------------------------|-----------------|
| `inquiryId` 누락          | 400 Bad Request |
| `pdId` 누락               | 400 Bad Request |
| `lnAmt` 누락 또는 0 이하      | 400 Bad Request |
| `inquiryId`에 해당하는 이력 없음 | 예외 (이력 없음)      |
| 조회 일자가 오늘이 아님 (만료)      | 예외 (재조회 필요)     |
| 선택한 `pdId`가 조회 결과에 없음   | 예외 (조회되지 않은 상품) |
| `lnAmt` > `maxLoanAmt`  | 예외 (한도 초과)      |

---

## 4. 시나리오 체크리스트

### 정상 시나리오

- [ ] 복수 상품 한도조회 → 모든 상품 결과 반환
- [ ] 단일 상품 한도조회 → 정상 결과 반환
- [ ] 한도조회 당일 대출신청 → 약정 생성 성공
- [ ] 신청 금액이 한도 이하인 경우 → 정상 처리

### 예외 시나리오

- [ ] 만료된 inquiryId로 신청 (다음날) → 재조회 안내 예외
- [ ] 존재하지 않는 inquiryId로 신청 → 이력 없음 예외
- [ ] 조회하지 않은 pdId로 신청 → 예외
- [ ] 신청 금액이 한도 초과 → 예외
- [ ] pdIds 빈 목록으로 조회 → 400
- [ ] lnAmt = 0으로 신청 → 400

---

## 5. 구현 체크리스트

### loan-svc — 인프라 (ACL)

- [ ] `ProductClient` 인터페이스 생성 (`getProduct(Long pdId): ProductInfo`)
- [ ] `ProductInfo` 값 객체 생성 (`pdId`, `maxLoanAmt`, `intrRt`)
- [ ] `InMemoryProductClient` 구현체 생성

### loan-api — 도메인 계약

- [ ] `LnInquiryResult` 인터페이스 생성 (`pdId`, `maxLoanAmt`, `intrRt`)
- [ ] `LnInquiry` 인터페이스 생성 (`inquiryId`, `custId`, `inquiryDt`, `getResults()`)

### loan-core — 도메인 구현

- [ ] `LnInquiryResultImpl` 구현
- [ ] `LnInquiryImpl` 구현
- [ ] `LnInquiryCreateSpec` 생성
- [ ] `LnInquiryRepository` 인터페이스 생성
- [ ] `InMemoryLnInquiryRepository` 구현

### loan-svc — 애플리케이션

- [ ] `LoanInquiryRequest` DTO 생성 (`custId`, `pdIds`)
- [ ] `LoanInquiryResultItem` DTO 생성
- [ ] `LoanInquiryResponse` DTO 생성
- [ ] `LoanApplyRequest` 수정 — `custId` 제거, `inquiryId` 추가
- [ ] `LoanCommandService.inquiry()` 구현
- [ ] `LoanCommandService.apply()` 수정 — 이력 참조, 유효기간·한도 검증
- [ ] `LoanController` — `POST /v1/loans/inquiry` 추가

### 검증

- [ ] `./gradlew loan-svc:build` 통과
