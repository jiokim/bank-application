# 대화 기록 — 2026-05-13

> 세션: `22018cbf-3785-432d-b6d0-3dff1853df09.jsonl`  
> 저장: 2026-05-13 10:19:21


---

## User

복잡한 비즈니스 로직(예: 금융권 대출, 결제 시스템 등)을 추상화 없이 구현할 때와 추상화 계층을 도입할 때의 트레이드오프와 실제 사례는?
    •  플랫폼/서비스 회사에서 추상화 계층 도입이 실제 유지보수, 확장성, 생산성에 미치는 영향은?
    •  복잡한 도메인(예: 대출, 결제)에서 추상화 남용 시 발생하는 오버엔지니어링, 성능 저하 사례는?
    •  추상화 계층 없이 구현 시 코드 중복, 비즈니스 로직 분산, 테스트 어려움 등 단점 극복 방법은?
    •  클린 아키텍처, DDD 등 원칙적 설계가 실제 서비스 개발에서 실패하는 구체적 이유는?
    •  고객사별 커스터마이징이 많은 프로젝트에서 추상화 전략과 실제 운영상의 문제점은?


---

## Assistant

이 프로젝트와 실제 금융 SI/플랫폼 경험을 바탕으로 구체적으로 답변합니다.

---

## 1. 추상화 없음 vs. 추상화 계층 — 트레이드오프

### 추상화 없이 구현할 때

```java
// 구체적이고 빠르게 짠 신용대출 한도 계산
public BigDecimal calcLimit(String custId) {
    NiceScore nice = niceClient.getScore(custId);    // 직접 호출
    KcbScore kcb  = kcbClient.getScore(custId);
    
    BigDecimal base = nice.getScore() > 700
        ? new BigDecimal("50000000")
        : new BigDecimal("20000000");
    
    if (kcb.getDelinquency()) base = base.multiply(new BigDecimal("0.5"));
    return base;
}
```

**장점:** 로직의 흐름이 한 파일에서 읽힌다. 온보딩 첫날에도 이해 가능.  
**단점:** 상품 추가 때마다 이 메서드가 커진다. 300줄이 되면 테스트 불가.

### 이 프로젝트의 추상화 — `LnInquiryStrategy`

```java
// loan-svc의 전략 패턴
public interface LnInquiryStrategy {
    String supportedPdId();
    LnInquiryResult inquire(LnInquiry inquiry, NiceScore nice, KcbScore kcb);
}
```

**실제 효과:** 상품 10개가 생겨도 `LoanApplicationService`는 건드릴 필요가 없다. 각 전략이 독립적으로 테스트된다.  
**비용:** 신규 개발자가 "이 계산이 어디서 일어나?" 를 따라가려면 인터페이스 → 구현체 → 등록 흐름을 세 파일에서 읽어야 한다.

**핵심 기준:** 변경이 자주 일어나는 축(여기선 상품 종류)에 추상화를 두면 이득. 변경이 없는 축에 두면 낭비.

---

## 2. 플랫폼/서비스 회사의 실제 영향

### 유지보수

| 상황 | 추상화 있음 | 추상화 없음 |
|------|------------|------------|
| 버그가 한 상품에만 있음 | 해당 전략만 수정 | 거대한 if-else에서 찾아서 수정 |
| 신용평가사 변경 | Client 인터페이스 구현체 교체 | 코드 전체에서 직접 호출 검색 |
| 신규 상품 추가 | 전략 클래스 하나 추가 | 기존 로직에 조건 삽입 → 회귀 위험 |

### 확장성

BXM 같은 프레임워크가 CustomerizationHook, BusinessDelegate 같은 추상화를 강제하는 이유가 여기 있다. 고객사마다 다른 로직을 동일한 인터페이스 뒤에 꽂아야 하기 때문. **플랫폼은 추상화가 생존 조건이다.**

### 생산성 — 역U자 곡선

```
  생산성
    │         ●
    │       ●   ●
    │     ●       ●
    │   ●           ●
    │ ●               ● ← 오버엔지니어링 구간
    └──────────────────── 추상화 수준
```

초기엔 추상화가 없어야 빠르다. 중간 지점(도메인이 안정된 후)에서 핵심 변동 축에만 추상화를 넣으면 생산성이 정점. 그 이후 계속 추상화를 쌓으면 오히려 느려진다.

---

## 3. 추상화 남용의 실제 사례

### 금융권에서 흔한 실패 패턴

**① Factory of Factory of Strategy**
```java
// 실제 본 코드 (익명 처리)
LnLimitCalculatorFactory factory = FactoryRegistry.get("CREDIT_LOAN");
LnLimitCalculatorStrategy strategy = factory.create(context);
LnLimitCalculatorResult result = strategy.execute(CalculatorInput.from(request));
```
한도 계산 하나 하는데 클래스 5개를 횡단해야 한다. `FactoryRegistry`가 어디서 `"CREDIT_LOAN"`을 주입받는지 따라가다 포기.

**② 과도한 제네릭**
```java
// 이게 어떤 T인지 호출부를 봐야만 알 수 있음
public <T extends Arr, R extends ArrResult<T>> R process(
    ArrProcessor<T, R> processor, ArrContext<T> context) { ... }
```
컴파일러를 위한 코드가 되어버림. IDE 없으면 읽기 불가.

**③ 성능 저하**
대출 실행 트랜잭션 하나에 AOP `@Around` 가 10개 겹쳐 있으면, 스택 트레이스가 60줄이 넘고 프로파일링 때 "내 코드가 어디 있나?" 를 찾기 어려워진다. 특히 `@Sensitive` 처리처럼 리플렉션 기반 추상화는 대용량 배치에서 병목이 된다.

---

## 4. 추상화 없이 구현할 때 단점 극복 전략

### 코드 중복 → 패키지 레벨 응집

추상화 없이도 **같은 패키지 안에 관련 로직을 모으면** 중복 범위를 제한할 수 있다.

```
loan-svc/
  com.bank.loan.credit/      ← 신용대출 관련만
    CreditLoanService.java
    CreditLimitCalculator.java
    CreditLoanValidator.java
  com.bank.loan.guarantee/   ← 보증대출 관련만
```

인터페이스 없어도 패키지가 경계 역할.

### 비즈니스 로직 분산 → `@Transactional` + 단일 서비스 메서드 원칙

로직이 여러 레이어에 새는 것은 추상화 수준의 문제가 아니라 **서비스 메서드가 다른 서비스를 너무 많이 호출하는** 설계 문제. 서비스 메서드 하나 = 유스케이스 하나 원칙이 더 실용적.

### 테스트 어려움 → 순수 함수 추출

클래스 추상화 대신 **static 순수 함수** 로 계산 로직 분리:
```java
// 이 정도면 인터페이스 없어도 테스트 가능
static BigDecimal calcLimit(int niceScore, boolean kcbDelinquency) { ... }
```

---

## 5. 클린 아키텍처/DDD가 실패하는 구체적 이유

### ① 도메인이 실제로는 DB 스키마다

금융 SI에서 "도메인 모델"은 종종 **기존 원장 테이블 스키마를 Java 클래스로 1:1 매핑한 것**이다. 테이블이 곧 Aggregate다. `LnArrImpl`이 추상 클래스인 이유도 결국 테이블 상속 구조 때문. 이 경우 도메인 모델이 인프라에 이미 종속되어 있어 클린 아키텍처의 전제(도메인이 인프라를 몰라야 한다)가 무너진다.

### ② 유비쿼터스 언어가 팀 내에서 합의 안 됨

DDD의 핵심은 개발자·기획자·도메인 전문가가 같은 단어를 쓰는 것인데, 금융 SI에서 기획서는 `대출약정`, 원장은 `LN_ARR`, Java는 `LnArr`, API는 `loanArrangement` 로 각자 다른 이름을 쓴다. 번역 비용이 지속적으로 발생.

### ③ Bounded Context 경계가 조직 경계와 다름

결제팀이 대출팀 테이블에 직접 쿼리하는 순간 BC가 붕괴된다. 이것은 설계 실패가 아니라 **조직 구조와 배포 구조가 분리되어 있지 않아서** 생기는 문제. 마이크로서비스 없이 DDD를 하면 결국 논리적 경계는 있지만 물리적으로는 모든 팀이 같은 DB를 본다.

---

## 6. 고객사 커스터마이징이 많은 프로젝트 (BXM류)

### 실제 운영 문제

**Hook 지옥:** 고객사마다 `BeforeLoanExecuteHook`, `AfterLoanExecuteHook` 구현체가 있는데, 고객사가 20개면 같은 훅이 20가지다. 버그 하나 고치면 20군데 검증 필요.

**버전 분기:** 고객사 A는 v2.3 인터페이스 기준으로 구현했는데, 플랫폼이 v3.0으로 인터페이스를 바꾸면 A의 구현이 깨진다. 인터페이스는 **확장엔 열려 있어야 하지만 축소엔 극도로 보수적**이어야 하는데 현실에선 이 규율이 무너진다.

**실용적 전략:**
```
플랫폼 제공                고객사 구현
─────────────────          ─────────────────
interface (절대 삭제 금지) → impl (고객사 소유)
abstract class (default    → extends (필요한 것만 override)
  impl 제공)
@ConditionalOnMissingBean  → @Bean (플랫폼 default 교체)
```

이 프로젝트의 `@ConditionalOnMissingBean` 패턴이 정확히 이것이다. 플랫폼이 기본 구현을 제공하고, 필요하면 서비스 모듈이 덮어쓴다.

---

## 요약

| 질문 | 실용적 답 |
|------|---------|
| 언제 추상화? | 변경 빈도가 높은 축이 2개 이상 명확해진 후 |
| 언제 안 함? | 프로토타입, 단일 상품, 팀 규모 3명 이하 |
| DDD 실패 이유 | DB/조직 구조가 먼저 있고, 설계가 나중에 오기 때문 |
| 커스터마이징 | `@ConditionalOnMissingBean` + 인터페이스 불변 원칙 |
| 핵심 원칙 | 추상화는 현재 중복의 제거가 아니라 **미래 변경의 격리**를 위해 |


---

## User

지금 이글에 대한 로그는 어디에 쌓여?


---

## Assistant

별도 로그 설정이 없습니다. **현재는 Spring Boot 기본값으로만 동작**하고 있습니다.

- **로그 위치:** 파일로 쌓이지 않고 **콘솔(stdout)에만** 출력됩니다.
- **로그 레벨:** INFO (Spring Boot 기본값)
- **포맷:** Spring Boot 기본 패턴 (`%d{HH:mm:ss.SSS} [thread] LEVEL logger : message`)
- `logback-spring.xml` 도 없고, `application.yml`에 `logging:` 섹션도 없습니다.

파일로 쌓고 싶다면 `application.yml`에 한 줄만 추가하면 됩니다:

```yaml
logging:
  file:
    name: logs/loan-svc.log   # 상대경로 → 실행 디렉터리 기준
```


---

## User

너랑 하는 대화를 그대로 저장하고 싶어


---

## Assistant

현재 대화가 `.jsonl` 파일로 자동 저장되고 있습니다.

**현재 대화 위치:**
```
~/.claude/projects/-Users-kimjio-Desktop-project-bank-application/22018cbf-3785-432d-b6d0-3dff1853df09.jsonl
```

`.jsonl` 은 JSON Lines 형식이라 그대로 읽기엔 불편합니다. 사람이 읽기 좋은 형태로 뽑으려면:

```bash
# 메시지 내용만 텍스트로 추출
cat ~/.claude/projects/-Users-kimjio-Desktop-project-bank-application/22018cbf-3785-432d-b6d0-3dff1853df09.jsonl \
  | jq -r 'select(.type == "say") | "\(.speaker): \(.text // (.content[]?.text // ""))"' \
  > ~/Desktop/conversation.txt
```

어떤 형태로 저장하고 싶으신가요?
- **텍스트 파일**로 그냥 읽기 편하게
- **마크다운**으로 정리해서 docs/ 에 보관
- **원본 jsonl** 그대로 백업


---

## User

지금 터미널에 나오는 그대로 정리하고 싶어


---

## Assistant

`docs/conversation-2026-05-13.md` 에 저장됐습니다.

앞으로 대화를 저장하고 싶을 때는 터미널에서 이 명령 하나로 됩니다:

```bash
! python3 ~/.claude/export-conversation.py
```

자주 쓸 것 같으면 스크립트로 만들어 두는 게 편할 것 같은데, 만들어 드릴까요?


---

## User

응 만들어줘
