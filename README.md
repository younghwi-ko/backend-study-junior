# GDGoC KU 5기 BE Junior 스터디

Java와 Spring Boot 백엔드 스터디의 회차별 학습 결과를 정리하는 저장소입니다. 기존 Spring Boot 프로젝트와 Git 커밋 이력을 유지하고, 3회차의 독립 Java/JDBC 프로젝트를 `week03/`에 추가했습니다.

## 회차별 학습 내용

| 회차/구분 | 위치 | 실제 코드에서 확인한 내용 |
| --- | --- | --- |
| 1·2회차 기존 실습 | 루트 `src/`, Gradle 프로젝트 | `/hello`, `/users` 예제, Java 모델과 컨트롤러·서비스 분리, 도서·수강·동아리 등의 실습 |
| 기존 확장 실습 | 루트 `src/` | 공연·예약 API, 수동/컨테이너/Spring DI 비교, 출석 관리 |
| 3회차 출석 과제 | [`week03/`](week03/README.md) | 상속·package·import, Docker PostgreSQL, DDL, JDBC 생성·조회, static 제거와 생성자 주입 |

기존 커밋에는 회차 태그가 없어 1회차와 2회차의 파일 경계를 임의로 나누지 않았습니다. 기존 확장 실습의 회차도 새로 지정하지 않았습니다.

## 저장소 구조 및 실행

```text
backend-study-junior/
  src/main/                 # 기존 Spring Boot 소스 및 H2 설정
  src/test/                 # 기존 테스트
  build.gradle.kts
  settings.gradle.kts
  gradle/, gradlew, gradlew.bat
  week03/                   # Gradle과 독립된 순수 Java/JDBC 실습
    src/Main.java
    src/animal/, src/club/
    lib/postgresql-42.7.13.jar
    sql/schema.sql, sql/practice.sql
    run.ps1, setup-db.ps1
    .env.example
    README.md, STEPS.md, VERIFICATION.md
```

`week03`은 Gradle 서브프로젝트가 아닙니다. 루트 Gradle 빌드는 기존 `src/main`과 `src/test`를 사용하며, 3회차는 `week03` 폴더에서 별도로 컴파일합니다. 기존 프로젝트 위치와 설정은 변경하지 않았습니다. VS Code로 3회차만 작업하려면 `week03` 폴더를 별도로 엽니다.

Windows PowerShell에서 기존 프로젝트:

```powershell
.\gradlew.bat test bootJar
.\gradlew.bat bootRun
```

3회차 문법 실습:

```powershell
cd week03
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -SyntaxOnly
```

3회차는 Java 17, PostgreSQL 18, Docker Desktop, JDBC 42.7.13을 사용합니다. 동아리 생성·조회와 회원 추가·조회 네 기능을 구현했습니다. DB 접속 비밀번호는 환경변수로 주입합니다. Docker/DDL 설치와 JDBC 실행 순서는 [week03/README.md](week03/README.md)를 참고하세요. 루트 Spring Boot의 H2 설정과 3회차 PostgreSQL은 서로 독립적입니다.

## 강의 기반 실습과 수행 작업

3회차 패키지·테이블·서비스·static 제거 방식은 [GDGoC KU 강의](https://www.youtube.com/watch?v=0IRndJJyQL4)와 [PPT](https://docs.google.com/presentation/d/119wp4B6HYOvqzaRB5Jv8e_bekQ5cjWZ3/edit)를 기반으로 구성했습니다. 사용자 직접 수행 작업은 Docker Desktop 설치·초기 설정과 과제 제출입니다. Java/SQL 작성, 실행 보조 스크립트, 실제 컴파일·DB 검증과 이번 GitHub 정리는 AI의 도움으로 진행했습니다. 강의 코드를 문자 단위로 복제했다고 주장하지 않습니다.

제출 당시의 원본 폴더와 ZIP은 저장소 밖에서 보존합니다. Public 저장소에는 비밀번호를 환경변수로 바꾼 복사본만 추가합니다. 실제 환경변수 파일, DB 데이터, 빌드 산출물, 제출 ZIP은 Git에 포함하지 않습니다. 구현하지 않은 Spring/JDBC 통합, 인증, 비밀번호 해시 기능은 추가하지 않았습니다.

## 검증 결과 (2026-10-08)

- 기존 Spring Boot: 전체 소스 컴파일 및 bootJar 생성 성공. 실행 JAR의 서버 기동과 `/hello`, `/users` HTTP 200 확인.
- 기존 테스트: 51개 중 42개 통과, 9개 실패. 동아리 응답의 비밀번호 노출, 수강 검증, 도서 영속화/실패 시 복구, 예약 동시성 항목에서 실패했습니다. 기존 소스와 테스트는 이번 정리에서 변경하지 않았으며 전체 테스트가 통과했다고 주장하지 않습니다.
- 3회차: 전체 Java 컴파일, 문법 예제, 환경변수 방식의 JDBC 동아리·회원 생성/조회 성공. DB 비밀번호 누락 오류 처리 확인.

아래는 기존 VS Code 환경 설정 및 Spring Boot 실행 안내입니다.

---

## 🛠 VS Code 개발 환경 설정 가이드

독립된 스터디 전용 프로필을 생성하여 쾌적한 개발 환경을 구축하는 방법입니다.

### 1. 스터디 전용 프로필 만들기 (최초 1회)
1. VS Code 좌측 하단 **⚙️ (설정 아이콘)** → **Profiles** → **Create Profile...** 클릭
2. 프로필 이름에 `Spring-Study` 입력 후 **[Create]** 클릭

### 2. 필수 확장 프로그램 자동 설치
* 이 프로젝트 폴더를 열면 우측 하단에 안내 팝업이 뜹니다.
* **"이 저장소에 대한 권장 확장 프로그램이 있습니다. 모두 설치하시겠습니까?"** → **[Install All]** 클릭

### 3. VS Code 하단 상태 표시줄 (Status Bar) 버튼 활용
`actboy168.tasks` 확장이 설치되면 하단 상태바의 버튼으로 손쉽게 명령을 실행할 수 있습니다:
* **`$(play) Boot Run`** (초록색): Spring Boot 서버 실행 (`./gradlew bootRun`)
* **`$(beaker) Run Tests`** (주황색): 전체 단위 테스트 실행 (`./gradlew test`)
* **`$(shield) Generate SBOM`** (파란색): CycloneDX SBOM 추출 (`./gradlew cyclonedxBom`)

---

## 📋 개발 환경 및 SBOM 버전 확인 가이드

스터디 진행 전, 본인의 개발 환경 및 라이브러리 버전이 표준 명세와 일치하는지 확인해 보세요.

### 1. 프로젝트 주요 버전 명세
* **Java / JDK**: Java 17 (JDK 17 LTS)
* **Spring Boot**: v4.1.0
* **Build Tool**: Gradle 9.5.1 (Wrapper)
* **SBOM Standard**: CycloneDX (v1.5 / v1.6 JSON)

### 2. 내 환경 및 라이브러리 버전을 맞춰보는 방법
```bash
# 1. 내 로컬 Java 및 Gradle 런타임 버전 확인
java -version
./gradlew --version

# 2. CycloneDX SBOM 생성 (의존성 라이브러리 버전 추출)
./gradlew cyclonedxBom

# 3. 생성된 SBOM에서 프로젝트 의존성 라이브러리 및 버전을 표준과 대조 확인
# 생성 경로: build/reports/cyclonedx/application.cdx.json
```

> 💡 **Tip**: 생성된 `application.cdx.json` 파일을 [SBOM Viewer (sbomviewer.com)](https://sbomviewer.com/) 사이트에 드래그 앤 드롭하면 시각적인 웹 화면으로 라이브러리 명세를 편리하게 조회할 수 있습니다.

---

## 🚀 프로젝트 실행 방법

```bash
# 1. 애플리케이션 실행 (Gradle)
./gradlew bootRun

# 2. CycloneDX SBOM 추출
./gradlew cyclonedxBom
```

* **서버 주소**: `http://localhost:8080`
* **CycloneDX SBOM 파일 위치**: `build/reports/cyclonedx/application.cdx.json`
