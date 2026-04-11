# Product 서비스

PF 대출 플랫폼의 대출 상품 등록 및 조회를 담당하는 서비스입니다.

## 모듈 구조

```
product-api    도메인 인터페이스 정의
product-core   도메인 구현체
product        애플리케이션 계층 (Controller, Service, DTO)
```

### product-api

도메인 계약을 정의합니다.

| 클래스 | 설명 |
|--------|------|
| `Pd` | 대출 상품 엔티티 인터페이스 |
| `PdMngr` | 애그리게이트 루트 접근 인터페이스 |

### product-core

도메인 구현체를 제공합니다.

| 클래스 | 설명 |
|--------|------|
| `PdImpl` | `Pd` 구현체 |
| `PdMngrImpl` | `PdMngr` 구현체 |

### product

애플리케이션 계층입니다.

| 클래스 | 설명 |
|--------|------|
| `ProductController` | REST API 엔드포인트 |
| `ProductQueryService` | 상품 조회 유스케이스 |
| `ProductCommandService` | 상품 등록 유스케이스 |

## API

| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/v1/products/{productId}` | 상품 단건 조회 |
| `GET` | `/v1/products` | 상품 목록 조회 |
| `POST` | `/v1/products` | 상품 등록 |

### 상품 등록

```
POST /v1/products
Content-Type: application/json

{
    "productName": "신용대출 A",
    "interestRate": 3.50
}
```

### 상품 단건 조회

```
GET /v1/products/1
```

## 실행

```bash
./gradlew product:bootRun
```

서버 포트: `9001`

## 테스트

```bash
./gradlew product:test
```
