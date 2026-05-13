# los-apps 도메인 분석 및 bank-application 이식 보고서

## 1. 조사 범위

- 원본: `/Users/kimjio/Desktop/project/los-apps`
- 대상: `/Users/kimjio/Desktop/project/bank-application`
- 조사일: 2026-05-08
- 목적: BXM/OMM 의존성은 제거하고, 운영 LOS의 도메인 지식, 설계 패턴, 업무 규칙만 Spring Boot 4 + Java 21 프로젝트로 이식한다.

확인한 주요 자료:

- `/Users/kimjio/Desktop/project/los-apps/CLAUDE.md`
- `/Users/kimjio/Desktop/project/los-apps/README.md`
- `/Users/kimjio/Desktop/project/los-apps/docs/*.md`
- `/Users/kimjio/Desktop/project/los-apps/UE/src/bankware/corebanking/assessment/subsidiary/business/AmLmtQrySubInfoProviderImpl.java`
- `/Users/kimjio/Desktop/project/los-apps/UE/src/bankware/corebanking/assessment/scrapping/bizprocessors/ScrgDataDSRDefaultProcessor.java`
- `/Users/kimjio/Desktop/project/los-apps/AR/src/bankware/corebanking/arrangement/arrangement/business/utility/ArrMapStsChngDfltImpl.java`
- `/Users/kimjio/Desktop/project/los-apps/PD/src/bankware/corebanking/product/product/business/PdBaseInrtQryProviderImpl.java`
- `/Users/kimjio/Desktop/project/los-apps/ASSvc/src/bankware/corebanking/collateral/asset/service/dto/SSBSASEA01001In.omm`
- `/Users/kimjio/Desktop/project/los-apps/ASSvc/src-gen/bankware/corebanking/collateral/asset/service/dto/SSBSASEA01001In.java`
- `/Users/kimjio/Desktop/project/los-apps/UE/testcases/test/ue/aa/TestAmLmtQrySubInfoProviderImpl_getLmtQrySubInfoByLnAplctnNbr.java`

## 2. los-apps 전체 구조 요약

`los-apps`는 Java 1.8 기반 BXM 4.0.5 프로젝트이며, `CLAUDE.md` 기준으로 기능 모듈, 서비스 모듈, 배치 모듈, 공통 API 모듈로 나뉜다.

| 계층 | 예 | 역할 |
| --- | --- | --- |
| Functional | `AC`, `AR`, `AS`, `AT`, `CA`, `CE`, `CI`, `CM`, `LC`, `LK`, `PD`, `SM`, `ST`, `SV`, `UE`, `XP` | 도메인 로직, BO/Provider/DAO/DSO |
| Service | `ASSvc`, `CASvc`, `CESvc`, `CMSvc`, `LCSvc`, `LKSvc`, `LNSvc`, `PDSvc`, `SMSvc`, `UESvc`, `XPSvc` | BXM 서비스 진입점, DTO, BizProc/Facade |
| Batch | `ACBat`, `ASBat`, `CABat`, `CEBat`, `CMBat`, `LCBat`, `LKBat`, `LNBat`, `PDBat`, `SMBat`, `UEBat`, `XPBat` | 스케줄/후행 배치 |
| Base API | `CBAPI` | 공통 인터페이스와 DTO |

특징:

- 모듈 간 직접 Gradle 프로젝트 의존이 아니라 interface JAR, service DTO JAR, `META-INF/bxm-application.xml` 런타임 설정으로 연결된다.
- DTO는 `.omm` 파일에서 `src-gen` Java로 생성된다.
- `testcases/`는 JUnit 5 형태지만 `CbpJUnitModuleTestSupport`, BXM endpoint, BXM header, `IOmmObject`에 강하게 묶여 있다.

## 3. 도메인 모듈 파악

| 모듈 | 패키지 근거 | 업무 도메인 해석 | bank-application 관련도 |
| --- | --- | --- | --- |
| `AC` | `accounting/balanceverification`, `chartaccount`, `generalledger`, `journalizing`, `settlement` | 회계, 계정과목, 총계정원장, 전표, 결산/정산 | 낮음 |
| `AR` | `arrangement/arrangement`, `condition`, `loan`, `relationship`, `transactional` | 약정/계약, 약정 조건, 대출 약정, 계약 관계, 거래성 약정 | 매우 높음: `arrangement-core`, `loan-core` |
| `AS` | `asset/asset`, `evaluation`, `insurance`, `securing`, `relationship` | 담보/자산, 담보 평가, 보험, 담보 설정 | 중간: 담보대출 확장 시 |
| `AT` | `actor/customer`, `staff`, `partner`, `department`, `role` | 고객/직원/파트너/조직 등 행위자 | 중간: 고객 모델 확장 시 |
| `CA` | `configurationadmin`, 각 도메인 `configuration`, `constant`, `enums` | 설정관리, 도메인별 구성/코드/검증 | 중간: 코드/설정 체계 참고 |
| `CE` | `creditevaluate/creditdata`, `cutoff`, `internal`, `kaiscreditevaluate` | 신용평가, 컷오프, 내부평가, 외부 신용정보 | 높음: 한도/DSR/심사 정책 |
| `CI` | `contactcenter/callback`, `campaign`, `evaluation`, `work` | 콜센터, 캠페인, 상담 업무 | 낮음 |
| `CM` | `applicationcommon/approval`, `common`, `database`, `document`, `file`, `operation` | 공통 업무, 승인, 영업일, 주소, 파일, 알림, 운영 | 높음: 공통 코드/영업일/승인 패턴 |
| `LC` | `loancollection`, `debtsettlement`, `legalaction`, `writeoff`, `revival` | 채권, 회수, 법적조치, 대손/상각, 부활 | 낮음~중간: 연체/회수 확장 시 |
| `LK` | `link/channel` | 채널/대외 연계 게이트웨이 | 높음: 외부 채널 Adapter 패턴 |
| `LN` | Functional `LN` 디렉토리는 없고 `LNSvc`, `LNBat` 중심 | 대출 실행, 계약, 상환, 조건변경, 지급, 철회 | 매우 높음: `loan-core`, `loan-svc` |
| `PD` | `product/product`, `condition`, `template` | 상품, 상품조건, 상품템플릿, 기준금리 | 매우 높음: `product-core` |
| `SM` | `support`, `report`, `business/batch` | 경영/지원관리, 리포트, 지원성 업무 | 낮음 |
| `ST` | `settlement/balance`, `calculation`, `cashflow`, `transaction`, `profitloss` | 정산, 잔액, 현금흐름, 손익, 거래 | 중간: 상환/정산 확장 시 |
| `SV` | `servicemanagement/controller`, `executor`, `configurator`, `uiconfigurator` | 서비스 관리, 실행/컨트롤러/화면 설정 | 낮음: BXM 운영 성격 |
| `UE` | `assessment/origination`, `credit`, `scrapping`, `subsidiary`, `channel` | 여신 사전/심사, 한도조회, DSR, 금리, 신용/스크래핑 | 매우 높음: 한도조회/심사 |
| `XP` | `loanexternalproxy/nice`, `kcb`, `van`, `etcinst`, `fsb` | 여신 대외 프록시, NICE/KCB/VAN/기관 연계 | 높음: 외부 연계 Adapter 패턴 |

## 4. bank-application과 겹치는 도메인

현재 `bank-application`의 핵심 모듈은 다음과 같다.

- `arrangement-core`: `Arr`, `ArrImpl`, `ArrSttsEnum`, `ArrTpEnum`
- `loan-core`: `LnArr`, `LnInquiry`, `LnInquiryResult`, repository
- `product-core`: `PdImpl`, `LnPdImpl`, `DpPdImpl`, `PdMngrImpl`, `LnPdMngrImpl`
- `loan-svc`: 한도조회/대출실행 API
- `product-svc`: 상품 등록/조회 API

직접 겹치는 los-apps 모듈:

| bank-application | los-apps | 이유 |
| --- | --- | --- |
| `arrangement-core` | `AR` | `AR/src/.../arrangement`가 약정 루트, 상태, 조건, 고객/부서 관계를 다룬다. |
| `loan-core`, `loan-svc` | `UE`, `UESvc`, `LNSvc`, `AR` | 대출신청/한도조회는 `UE`, 실행/계약/상환 서비스는 `LNSvc`, 약정 원장은 `AR`에 걸쳐 있다. |
| `product-core`, `product-svc` | `PD`, `PDSvc`, `CA` | 상품/상품조건/상품템플릿은 `PD`, 서비스 DTO는 `PDSvc`, 설정/코드는 `CA`가 보조한다. |
| 공통/운영 | `CM`, `CBAPI` | 영업일, 승인, 공통 코드, 공통 예외/DTO 계약의 참고 대상이다. |

## 5. 가져올 수 있는 것

### 5.1 한도조회 도메인 모델

가장 먼저 이식할 가치는 `UE`의 한도조회 모델이다. 기존 문서 `springboot-limit-query-design.md`, `limit-query-trace-diagram.md`, `am-domain-limit-query-boundary-review.md`는 다음 경계를 명확히 한다.

```text
LoanApplication
  1:N LimitQuery
LimitQuery
  1:N LimitQueryItem
LimitQueryItem
  0..1 LimitQueryResult
  0..N LimitQueryExecution
```

운영 LOS의 실제 용어:

- `LN_APLCTN_NBR`: 대출신청번호
- `LMT_QRY_NBR`: 한도조회번호
- `AM_LMT_QRY_SUB_S`: 상품별 한도조회 보조/결과성 현재 테이블
- `AM_LMT_QRY_SUB_H`: 한도조회 결과 이력
- `PD_GRP_LRCL_CD`, `PD_GRP_MDCL_CD`: 상품그룹 대/중분류
- `LN_ABL_AMT`: 대출가능금액
- `LMT_QRY_RSLT_INRT`: 한도조회결과금리
- `RJCT_RSN*_CD`: 거절사유코드

Spring Boot 모델로 옮길 때는 BXM 테이블명 그대로가 아니라 아래처럼 도메인 이름을 정제하는 편이 좋다.

```java
class LoanApplication {
    String loanApplicationNo;
    String customerId;
    ApplicationStatus status;
}

class LimitQuery {
    String limitQueryNo;
    String loanApplicationNo;
    LimitQueryStatus status;
}

class LimitQueryResult {
    String productCode;
    BigDecimal limitAmount;
    BigDecimal interestRate;
    BusinessResultStatus businessResultStatus;
    String rejectReasonCode;
}
```

### 5.2 업무 결과 상태와 기술 실행 상태 분리

`los-apps/docs/springboot-limit-query-design.md`와 `limit-query-trace-diagram.md`의 핵심은 “정상 업무 거절”과 “기술 실패/타임아웃”을 분리하는 것이다.

이식 후보:

```text
업무 결과 상태:
- APPROVED
- REJECTED
- NO_LIMIT

기술 실행 상태:
- READY
- RUNNING
- SUCCESS
- FAIL
- TIMEOUT
```

이 구조는 bank-application의 `loan-core`에 이미 있는 `LnInquiry`, `LnInquiryResult`를 확장하기 좋다. 특히 멀티 상품 한도조회에서는 상품 A는 승인, 상품 B는 업무 거절, 상품 C는 타임아웃일 수 있으므로 결과와 실행 상태를 한 enum에 섞으면 안 된다.

### 5.3 상태 전이 규칙

`AR/src/bankware/corebanking/arrangement/arrangement/business/utility/ArrMapStsChngDfltImpl.java`는 약정 상태 전이표를 코드로 가지고 있다.

대표 흐름:

```text
APPLIED -> APPROVED / REJECTED / TERMINATED / CANCELED
APPROVED -> ACTIVE / REJECTED / WITHDRAWN / CONFIRMED / CANCELED
CONFIRMED -> ACTIVE / APPROVED / REJECTED / WITHDRAWN / CANCELED
ACTIVE -> TERMINATED / WITHDRAWN / CANCELED
REJECTED -> APPLIED / APPROVED
PURCHASE -> ACTIVE
```

현재 `bank-application/arrangement-core/src/main/java/com/bank/arrangement/core/domain/enums/ArrSttsEnum.java`는 `ACTIVE`, `TERMINATE` 정도의 단순 모델이다. `AR`의 상태 전이표를 직접 복사하지 말고, Spring 도메인 서비스로 재해석하는 것이 좋다.

```java
public interface ArrangementStatusPolicy {
    boolean canTransit(ArrangementStatus from, ArrangementStatus to);
}
```

### 5.4 DSR 적재/계산 입력 모델

`UE/src/.../ScrgDataDSRDefaultProcessor.java`에서 가져올 수 있는 것은 BXM 호출 방식이 아니라 DSR 산출에 필요한 입력 필드와 스킵 규칙이다.

확인된 규칙:

- 계산결과가 없거나 `0` 이하이면 DSR 적재를 스킵한다.
- DSR 기본정보는 대출신청번호, 심사식별자, 소득구분, 소득종류, 소득서류구분, 연간총소득금액을 중심으로 만든다.
- 기존 DSR 정보가 있으면 수정하고, 없으면 고객/액터/심사/한도조회 정보를 조합해 신규 등록한다.
- 한도조회 정보에서 신청금액, 상환방법, 대출기간, 접수채널을 가져온다.
- 대출조사 정보에서 금리결합등급, 적용이율을 가져온다.

Spring Boot 이식 형태:

```java
record DsrCalculationInput(
    String loanApplicationNo,
    String customerId,
    BigDecimal annualIncomeAmount,
    BigDecimal requestedAmount,
    String repaymentMethodCode,
    int loanTermMonths,
    BigDecimal appliedInterestRate
) {}
```

### 5.5 상품/금리 정책 패턴

`PD/src/.../PdBaseInrtQryProviderImpl.java`는 기준금리 조회에서 다음 규칙을 보여준다.

- 기관코드가 없으면 context의 기관코드를 사용한다.
- 적용시작일자/종료일자를 필수 검증한다.
- 날짜 형식과 시작일 <= 종료일을 검증한다.
- 기준금리 목록은 next key 방식으로 페이지 처리한다.
- 출력 DTO는 내부 조회 결과에서 필요한 필드만 다시 조립한다.

이식할 내용은 `CbbApplicationContext`, `PdUtil`, `CPD01` 자체가 아니라 “금리 기준일/적용기간/상품조건을 명시적으로 검증하는 정책”이다.

```java
class BaseInterestRateQuery {
    LocalDate applyStartDate;
    LocalDate applyEndDate;
    String baseInterestRateKindCode;
}
```

### 5.6 Provider, Adapter, Facade 패턴

소스 전반의 구조는 다음으로 반복된다.

```text
*Svc/service        BXM 서비스 진입점
bizproc            유스케이스 조립
business/*Provider 업무 기능 단위 Provider
dao/dso            DBIO 호출
proxy/adapter      다른 모듈 또는 외부 연계 호출
dto, interfaces    모듈 간 계약
```

Spring Boot로 옮길 때 권장 매핑:

| BXM 패턴 | Spring Boot 대체 |
| --- | --- |
| `@BxmService` service | `@RestController` + application service |
| BizProc | use case service |
| Provider | domain service 또는 policy component |
| DSO/DAO | Spring Data Repository / MyBatis mapper |
| Proxy | outbound port + adapter |
| OMM DTO | request/response DTO, command, record |

### 5.7 테스트 패턴에서 가져올 것

`testcases/`는 그대로 가져오기는 어렵지만, 다음은 참고할 만하다.

- 서비스코드/모듈별 테스트 파일 네이밍: `SSBSUE...`, `Test..._case001`
- 필수 header/context를 명시적으로 구성한다.
- 대표 업무 식별자 값을 고정해 회귀 테스트한다.
- 플랫폼 에러, 모듈 에러, 알 수 없는 에러를 분리해서 다룬다.

Spring Boot에서는 다음으로 바꾸는 것이 좋다.

```java
@Nested
class LimitQueryStatusTransitionTest {
    @Test
    void reject_business_result_should_not_be_technical_failure() {
        // given, when, then
    }
}
```

## 6. 가져올 수 없는 것

| 항목 | 예 | 이식 불가 이유 |
| --- | --- | --- |
| `.omm` DSL 자체 | `SSBSASEA01001In.omm` | BXM 전용 DTO DSL이며 Lombok/record/Bean Validation과 맞지 않는다. |
| `src-gen` OMM Java | `implements IOmmObject, Predictable, FieldInfo` | `bxm.omm.*`, JAXB, `@BxmOmm_Field`, isSet 플래그에 종속된다. |
| BXM annotation | `@BxmBean`, `@BxmCategory`, `@BxmService` | Spring Bean/Controller/Service 체계로 대체해야 한다. |
| BXM context | `CbbApplicationContext`, `CmnContext.setHeaderColumn` | 요청 context, audit column, tenant/institution 처리를 Spring 필터/AOP/엔티티 감사로 재설계해야 한다. |
| BXM 예외 | `BizApplicationException("AAPCME0006", ...)` | 메시지코드 체계는 참고 가능하지만 예외 클래스는 공통 도메인 예외로 재정의해야 한다. |
| BXM 비동기 실행 | `CbbServiceExecutor.executeAsyncForWait`, `AsyncResponse.waitForResponse` | Spring `TaskExecutor`, `CompletableFuture`, Resilience4j timeout/bulkhead 등으로 재구현해야 한다. |
| DBIO/DSO 호출 | `*_Dso.select...`, `@BXMType BeanCall` | DB 접근 기술에 종속된다. SQL/조회 의도만 추출한다. |
| 테스트 실행 기반 | `CbpJUnitModuleTestSupport`, 원격 `/serviceEndpoint/obj` | 로컬 단위/통합 테스트가 아니라 BXM 런타임/원격 endpoint 의존 테스트다. |
| interface JAR 운영 방식 | `intrfc`, service DTO JAR, reflibs | Gradle 멀티모듈 직접 의존 또는 명시적 API 모듈로 대체한다. |

OMM 비교 예시:

```text
OMM:
String custId<length=15 align=left description="고객식별자" encrypt="N">;
```

생성 Java는 다음처럼 BXM 메타데이터와 상태 추적 필드를 포함한다.

```java
public class SSBSASEA01001In implements IOmmObject, Predictable, FieldInfo {
    private boolean isSet_custId = false;

    @BxmOmm_Field(description="고객식별자", length=15, encrypt="N")
    private String custId;
}
```

Spring Boot에서는 다음 정도로 충분하다.

```java
public record CollateralSearchRequest(
    @Size(max = 15) String customerId,
    @Size(max = 2) String assetCustomerRelationCode,
    @Size(max = 2) String assetTypeCode
) {}
```

## 7. 이식 우선순위 Top 3

### 1순위. 한도조회 Aggregate 재설계

대상:

- `loan-core`
- `loan-svc`
- 기존 `LnInquiry`, `LnInquiryResult`

이유:

- 현재 bank-application이 “한도조회/대출실행”을 전면 기능으로 표방한다.
- `los-apps`의 가장 값진 운영 지식은 `LN_APLCTN_NBR`, `LMT_QRY_NBR`, 상품별 결과/이력/거절사유를 분리한 점이다.
- 멀티상품 한도조회는 단순 CRUD가 아니라 도메인 경계 설계가 필요한 기능이다.

권장 작업:

- `LoanApplication`, `LimitQuery`, `LimitQueryItem`, `LimitQueryResult`, `LimitQueryExecution` 도입
- 업무 결과 상태와 기술 실행 상태 enum 분리
- 한도조회번호와 대출신청번호를 GUID와 분리

### 2순위. 약정 상태 전이 정책

대상:

- `arrangement-core`
- `ArrSttsEnum`
- `Arr` 상태 변경 메서드

이유:

- 운영 LOS의 `AR`은 약정 상태 전이를 별도 map/policy로 본다.
- bank-application의 현재 상태 모델은 너무 단순해서 대출신청, 승인, 확정, 실행, 거절, 철회, 종료를 설명하기 어렵다.
- 상태 전이는 테스트로 고정하기 쉽고, BXM 의존 없이 도메인 규칙만 옮기기 좋다.

권장 작업:

- `APPLIED`, `APPROVED`, `CONFIRMED`, `ACTIVE`, `REJECTED`, `WITHDRAWN`, `TERMINATED`, `CANCELED` 도입
- `ArrangementStatusPolicy` 단위 테스트 작성
- 상태 변경 이력 모델 검토

### 3순위. 상품/금리/DSR 정책 컴포넌트화

대상:

- `product-core`
- `loan-core`
- 향후 `credit` 또는 `assessment` 패키지

이유:

- `UE`, `CE`, `PD`, `AR`에 흩어진 한도, 금리, DSR, 상품조건은 금융 도메인 핵심 규칙이다.
- BXM 코드를 그대로 옮기면 테스트 불가능한 서비스 덩어리가 되기 쉽다.
- 정책 객체로 분리하면 JUnit 5로 정상/거절/예외 케이스를 빠르게 검증할 수 있다.

권장 작업:

- `DsrPolicy`, `LimitCalculationPolicy`, `InterestRatePolicy`, `ProductEligibilityPolicy` 도입
- DSR 입력 모델을 record로 정의
- 거절사유코드 산출을 별도 policy로 분리

## 8. 구체적 이식 후보 목록

| 후보 | 원본 근거 | 대상 위치 | 이식 방식 |
| --- | --- | --- | --- |
| 대출신청번호/한도조회번호 생명주기 | `docs/am-domain-limit-query-boundary-review.md` | `loan-core/domain/model` | 새 Aggregate로 구현 |
| 상품별 한도조회 결과 | `AM_LMT_QRY_SUB_S`, `AmLmtQrySubInfoProviderImpl` | `LnInquiryResult` 재설계 | 결과/거절사유/금리/한도 필드 반영 |
| 한도조회 결과 이력 | `AM_LMT_QRY_SUB_H` | `LimitQueryResultHistory` | 선택적 도입 |
| 업무/기술 상태 분리 | `docs/springboot-limit-query-design.md` | `loan-core/domain/enums` | enum 2개로 분리 |
| 약정 상태 전이표 | `ArrMapStsChngDfltImpl` | `arrangement-core/domain/policy` | policy + 테스트 |
| 기준금리 조회 검증 | `PdBaseInrtQryProviderImpl` | `product-core/domain/service` | 날짜/기간/페이징 규칙 재구현 |
| DSR 입력/스킵 규칙 | `ScrgDataDSRDefaultProcessor` | `loan-core/domain/policy` | pure Java policy |
| 외부 연계 Adapter | `LK`, `XP`, `UESvc/channel/proxy` | `loan-svc/infrastructure` | outbound port/adapter |
| 테스트 케이스 네이밍 | `testcases/*_case001.java` | `src/test/java` | JUnit 5 nested/case 기반 |

## 9. 결론

`los-apps`에서 가져올 핵심은 BXM 코드가 아니라 도메인 경계다. 특히 `AR=약정`, `UE=사전심사/한도조회`, `PD=상품`, `LNSvc=대출 실행/상환`, `LK/XP=채널/대외 연계`의 책임 분리는 bank-application의 모듈 구조와 잘 맞는다.

반대로 `.omm`, `src-gen`, `CbbApplicationContext`, `CbbServiceExecutor`, `CbpJUnitModuleTestSupport`는 BXM 런타임을 전제로 하므로 제거 대상이다. Spring Boot 프로젝트에는 업무 식별자, 상태 전이, 정책 계산, Adapter 경계, 테스트 케이스 관점만 선별해서 옮기는 것이 맞다.

가장 실용적인 첫 작업은 `loan-core`의 한도조회 모델을 `LoanApplication -> LimitQuery -> LimitQueryItem -> Result/Execution` 구조로 바꾸는 것이다. 그 다음 `arrangement-core`에 상태 전이 정책을 넣고, 마지막으로 `product-core`/`loan-core`에 금리, DSR, 상품적격성 정책을 분리하면 운영 LOS의 설계 지식을 BXM 없이 흡수할 수 있다.
