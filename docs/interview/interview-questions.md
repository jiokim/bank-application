# 면접 질문 정리

## Gradle / 빌드

**Q. 멀티모듈 프로젝트에서 `api`와 `implementation`의 차이는?**
- `api`: 의존성이 소비자 모듈까지 전이됨. `loan-core`가 `arrangement-core`를 `api`로 선언하면 `loan-svc`도 `Arr` 인터페이스를 사용할 수 있음
- `implementation`: 내부 구현용. 소비자에게 노출되지 않아 불필요한 의존성 전이를 차단

**Q. Convention Plugin이란 무엇이고 `allprojects` 블록과 무엇이 다른가?**
- `allprojects`: 루트 `build.gradle`에 모든 모듈 공통 설정을 인라인으로 작성. 모듈이 늘수록 파일이 비대해지고 관심사가 뒤섞임
- Convention Plugin: 설정을 `.gradle` 파일로 분리해 플러그인으로 등록. 컴파일·캐시 가능하며 모듈 `build.gradle`이 `id 'bank.spring-library'` 한 줄로 끝남

**Q. Composite Build(`includeBuild`)는 무엇이고 왜 쓰나?**
- 별도의 독립된 Gradle 빌드를 현재 빌드에 포함하는 방식
- `build-logic`을 분리하면 플러그인 코드가 자신만의 의존성을 가질 수 있고, 플러그인 자체가 Gradle 캐시 대상이 되어 설정 속도가 빨라짐

**Q. BOM(Bill of Materials)이란?**
- 호환 가능한 라이브러리 버전 목록. Spring Boot BOM을 import하면 `spring-context`, `jackson` 등의 버전을 직접 명시하지 않아도 됨
- `build-logic/build.gradle`의 Spring Boot 플러그인 버전 하나가 전체 모듈의 Spring 의존성 버전을 결정

**Q. `-core` 모듈에서 `bootJar`를 비활성화하는 이유는?**
- `-core`는 단독 실행 모듈이 아니라 `-svc`에 포함되는 라이브러리
- `bootJar`를 켜두면 실행 가능한 fat JAR가 생성되어 `java-library`로 사용할 수 없음

---

## 모듈 설계

**Q. `-api` 모듈을 별도로 분리하는 이유는?**
- 인터페이스(계약)와 구현을 물리적으로 분리해 컴파일 타임에 은닉성을 보장
- 소비자가 `-api`에만 의존하면 `-core`의 구현 클래스를 실수로 참조하는 것이 원천 차단됨

**Q. 순수 인터페이스 모듈(`product-api`)에 Spring 의존성을 넣지 않은 이유는?**
- `product-api`는 Java 인터페이스만 존재하므로 Spring Context가 필요 없음
- `bank.spring-library` 대신 `bank.java-common`을 사용해 불필요한 Spring 의존성을 제거
- Spring 없이 가벼운 계약 모듈로 유지해야 다른 환경에서도 재사용 가능

**Q. 모듈이 많아질수록 생기는 문제와 해결 방법은?**
- 문제: `settings.gradle` 관리, Gradle 설정 반복, IDE 인덱싱 부하
- 해결: Convention Plugin으로 설정 DRY 유지. 새 모듈 추가 비용을 `build.gradle` 3줄 수준으로 낮춤

---

## Spring Boot

**Q. `@AutoConfiguration`과 `@ConditionalOnMissingBean`을 함께 쓰는 이유는?**
- `@AutoConfiguration`: 라이브러리가 Spring Boot 앱에 포함될 때 자동으로 빈을 등록
- `@ConditionalOnMissingBean`: 소비자(`-svc`)가 같은 타입의 빈을 직접 등록하면 자동 등록을 건너뜀
- 둘을 조합하면 기본 구현은 자동 제공하되 필요시 오버라이드 가능한 구조가 됨

**Q. 멀티모듈에서 의존성 버전을 어떻게 일관되게 관리하나?**
- Spring Boot BOM을 통해 Spring 생태계 라이브러리 버전을 일괄 관리
- BOM 버전은 `build-logic/build.gradle` 한 곳에서만 선언. 업그레이드 시 이 파일만 수정하면 전체 모듈에 적용됨

---

## 모듈 참조 방식 — project() vs JAR

**Q. 멀티모듈에서 모듈 간 참조 방식 두 가지를 설명하고 차이점은?**
- `project(':arrangement-core')`: Gradle이 의존 그래프를 인식해 빌드 순서를 자동 결정. 같은 Gradle 빌드 안에 있는 모듈에만 사용 가능
- JAR 참조 (`implementation 'com.bank:arrangement-core:1.0.0'`): Gradle 입장에서 외부 라이브러리와 동일. 같은 저장소에 있어도 사용 가능하지만 의존 그래프를 그릴 수 없어 빌드 순서를 사람이 직접 관리해야 함

**Q. JAR 참조 방식이 유리한 경우는?**
- 팀이 분리되어 모듈별로 다른 배포 주기를 가질 때
- 모듈이 안정화되어 버전을 고정하고 여러 팀이 독립적으로 소비할 때
- 저장소 자체가 분리된 경우 (`project(':...')` 참조 불가)

**Q. 여신팀과 수신팀이 분리되어 있을 때 공통 모듈(`arrangement-core`)을 어떻게 관리하나?**
- `arrangement-core`를 별도 저장소로 분리해 버전 관리 후 사내 Maven 레지스트리에 발행
- 각 팀은 버전을 JAR로 참조하고, 업그레이드 시점을 독립적으로 결정
- 인터페이스(`Arr`)가 안정적일수록 버전 충돌 없이 오래 유지됨

**Q. 현재 프로젝트는 왜 `project(':...')` 방식을 선택했나?**
- 단일 저장소, 소규모 팀 구조에서는 `project(':...')` 방식이 개발 속도에 유리
- JAR 발행 방식은 변경마다 버전 올리고 발행 → 다른 모듈에서 버전 업데이트 과정이 추가되어 사이클이 느려짐
- 다만 `arrangement-core`를 인터페이스 중심으로 설계해두어, 팀이 분리되는 시점에 JAR 발행 방식으로 전환하는 비용을 낮춰둠

---

## 레거시 비교 (los-app 경험 기반)

**Q. 레거시 빌드 시스템과 비교해 개선한 점은?**
- 동적 버전(`+`) + 캐시 0초 → BOM 고정 버전으로 결정론적 빌드
- 로컬 JAR 의존성(`lib/`, `bxm/`) → Maven Central 단일 출처
- 모듈 간 의존성이 Gradle에 선언되지 않아 빌드 순서를 쉘 스크립트로 직접 관리 → 의존성을 명시적으로 선언해 Gradle이 순서를 자동 결정
- `ignoreFailures = true` → 테스트 실패 시 빌드 실패로 품질 보장