# Bank Application Docs

프로젝트 문서는 읽는 목적에 따라 디렉터리를 분리한다.

## 문서 구조

```text
docs/
├── README.md
├── architecture/
│   ├── domain-model.md
│   ├── common-framework-design.md
│   └── adr/
│       └── 000-docs-structure.md
├── build/
│   ├── build-structure.md
│   └── gradle-multimodule-optimization.md
├── features/
│   ├── loan-inquiry-flow.md
│   └── pre-service-sensitive-encryption.md
├── guides/
│   ├── README.md
│   └── pre-service-sensitive-encryption-spring-guide.md
├── interview/
│   └── interview-questions.md
└── roadmap/
    └── feature-expansion-plan.md
```

## 카테고리

### Architecture

독자: 면접관, 의사결정자. "왜 이렇게 설계했는가"를 설명하는 문서.

- [도메인 모델 설계](architecture/domain-model.md)
- [금융 공통 처리 구조 Spring Boot 재구현 설계](architecture/common-framework-design.md)
- [설계 결정 기록 (ADR)](architecture/adr/000-docs-structure.md)

### Features

독자: 온보딩하는 팀원. "이 기능이 뭔지, 어떻게 쓰는지"를 설명하는 문서.

- [대출 한도조회 서비스 흐름](features/loan-inquiry-flow.md)
- [PreService 민감정보 암호화 공통 기능](features/pre-service-sensitive-encryption.md)

### Guides

독자: 본인. 학습과 설명 연습을 위한 문서. "왜 이 기술이 이렇게 동작하는가"가 중심.

features/와 같은 기능을 다뤄도 관점이 다르면 중복이 아니다. 작성 원칙은 [guides/README.md](guides/README.md) 참고.

- [PreService 민감정보 암호화 설계 설명](guides/pre-service-sensitive-encryption-spring-guide.md)

### Build

Gradle 멀티모듈 구성, convention plugin, 빌드 최적화 관련 문서.

- [빌드 구조 위키](build/build-structure.md)
- [멀티모듈 Gradle 빌드 최적화](build/gradle-multimodule-optimization.md)

### Roadmap

앞으로 확장할 기능과 설계 후보를 정리하는 문서.

- [기능 확장 계획](roadmap/feature-expansion-plan.md)

### Interview

면접 대비용 질문과 답변을 정리하는 문서.

- [면접 질문 정리](interview/interview-questions.md)

## 작성 기준

| 질문 | 디렉터리 |
|---|---|
| 왜 이렇게 설계했나, 어떤 결정을 했나 | `architecture/` 또는 `architecture/adr/` |
| 이 기능이 뭔지, 어떻게 쓰는지 | `features/` |
| 왜 이 기술이 이렇게 동작하는지 | `guides/` |
| Gradle, 빌드 구성 | `build/` |
| 앞으로 할 일 | `roadmap/` |

새 문서를 만들기 전에 "이 문서의 독자는 누구인가"를 먼저 정의한다.
