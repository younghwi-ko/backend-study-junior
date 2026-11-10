# Spring Initializr 생성 기록

생성일: 2026-11-10 (Asia/Seoul)

공식 Spring Initializr에서 ZIP 템플릿을 다운로드하고 그 위에서 작업했습니다.

```text
https://start.spring.io/starter.zip
type=gradle-project-kotlin
language=java
bootVersion=4.1.2
baseDir=week04
groupId=com.gdgku
artifactId=week04
name=week04
description=GDGoC KU Backend Week 4
packageName=com.gdgku.week04
packaging=jar
javaVersion=17
dependencies=web,jdbc,validation,postgresql,h2
```

생성된 Gradle Wrapper와 버전 설정을 유지했습니다. 내려받은 원본 ZIP의 SHA-256은 다음과 같습니다.

```text
FD8AF802C896120656242E259D992C13678A7C4314C2161C4D1C793BFD18C5FE
```

생성 템플릿 대비 변경:

- H2 콘솔은 사용하지 않으므로 `spring-boot-h2console` 의존성을 제거했습니다. H2 드라이버는 유지했습니다.
- Club/Member와 동물 예제를 이전하고 Spring 컴포넌트, JDBC 저장소, API, 데모 실행기를 작성했습니다.
- DB 프로필·DDL·실제 기능을 검증하는 통합 테스트와 실행 문서를 추가했습니다.
- 내용 없는 기본 `contextLoads` 테스트는 기능 통합 테스트로 대체했습니다.
- Git 제외 규칙에 DB 데이터·비밀번호 설정 파일·로그를 추가했습니다.

기존 저장소의 소스·테스트·Gradle 설정과 `week03`는 유지하고, 별도 `week04` 폴더로 추가합니다.
