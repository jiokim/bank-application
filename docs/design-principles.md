# 설계 원리 및 원칙

## 1. 모듈 경계 원칙

`-api`는 도메인 간 계약, `-core`는 도메인 내부 구현, `-svc`는 실행 단위

| 모듈 | 역할 | 담는 것 |
|------|------|---------|
| `-api` | 도메인 간 계약 | 데이터 인터페이스(`Pd`), 서비스 인터페이스(`PdMngr`) |
| `-core` | 도메인 내부 구현 | `Impl` 클래스, Repository, 빈 등록 |
| `-svc` | 실행 단위 | Controller, Application Service, ACL |

도메인 경계를 넘을 때는 `-api`만 참조한다. `-core`(구현)는 참조하지 않는다.

---

## 2. 인터페이스 계층 — 상속으로 확장

계약(인터페이스)은 상속으로 확장한다.

```
PdMngr (product-api)
  └── LnPdMngr (loan-core)
  └── DpPdMngr (deposit-core)
```

확장이 예상되는 인터페이스는 `-api`에 두어야 타 도메인이 상속할 수 있다.

---

## 3. 구현 계층 — 위임으로 결합 차단

구현은 상속 대신 위임(Delegation)으로 확장한다.

```java
public class LnPdMngrImpl implements LnPdMngr {
    private final ProductClient productClient;

    public Pd getPd(Long pdId) {
        return productClient.getProduct(pdId); // 위임
    }
}
```

구현 상속(`extends PdMngrImpl`)은 모듈 간 컴파일 의존과 깨지기 쉬운 기반 클래스 문제를 만든다.

---

## 4. 의존 방향 원칙

컴파일 타임에는 계약만 알고, 구현체 연결은 런타임(DI)에 맡긴다.

```
loan-core  ──▶ product-api   (계약, 컴파일)
loan-svc   ──▶ loan-core
loan-svc   ✗──▶ product-core  (구현, 차단)
```

- `api` 선언 = 전이 의존 (사용하는 쪽까지 노출)
- `implementation` 선언 = 전이 차단 (내부에서만 사용)

---

## 5. ACL (Anti-Corruption Layer)

외부 서비스의 변화로부터 내부 도메인을 보호한다.

```
LoanCommandService → LnPdMngr
                         └── LnPdMngrImpl → ProductClient (ACL 포트)
                                                └── InMemoryProductClient
                                                └── HttpProductClient (미래)
```

`ProductClient`가 반환하는 `ProductInfo implements Pd` — 외부 표현을 내부 도메인 타입으로 변환하는 것이 ACL의 역할이다.

---

## 6. Domain Service의 위치

프레임워크 오염 금지 대상은 Entity/VO이며, Domain Service는 타협 가능하다.

| 계층 | 클래스 예시 | `@Component` |
|------|-----------|-------------|
| Entity / VO | `PdImpl`, `LnArrImpl` | 절대 금지 |
| Domain Service | `PdMngrImpl` | 타협 허용 |
| Infrastructure | `InMemoryProductRepository` | 자유 |

Domain Service는 Repository를 주입받아야 하므로 DI 프레임워크 의존이 불가피하다.

---

## 7. 패키지 구조 원칙

도메인이 패키지 최상위, 역할이 하위 패키지.

```
com.bank.product              ← svc (Spring 스캔 루트)
com.bank.product.api.*        ← api (빈 없음)
com.bank.product.core.*       ← core (@Component 스캔됨)
  domain.model/               ← Entity / VO  (프레임워크 금지)
  domain.service/             ← Domain Service (@Component 허용)
  domain.repository/          ← Repository 인터페이스
  infrastructure.repository/  ← Repository 구현체
```

`@SpringBootApplication`은 선언된 패키지와 그 하위 패키지를 스캔한다.

---

## 8. Shared Kernel (공유 커널)

억지로 의존을 끊으려다 복잡도만 높아질 수 있다.

`arrangement-core`는 모든 약정 도메인의 공통 기반이다.
`loan-core → arrangement-core` 의존은 기술적 선택이 아니라 도메인의 본질이므로 허용한다.

```
-core → 자기 -api          ✓
-core → arrangement-core   ✓ (Shared Kernel)
-core → 다른 도메인 -core   ✗
```

---

## 9. AutoConfiguration vs `@Component`

| 등록 방식 | 대상 | 이유 |
|----------|------|------|
| `@AutoConfiguration` + `@ConditionalOnMissingBean` | `InMemoryProductRepository` | 교체 가능한 인프라 기본값 |
| `@Component` | `PdMngrImpl` | 항상 존재해야 하는 도메인 서비스 |