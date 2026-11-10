# GDGoC KU BE Junior 4회차 — Spring과 JdbcTemplate

3회차의 순수 Java·PostgreSQL JDBC 프로젝트를 Java 17, Spring Boot, Gradle 프로젝트로 옮겼습니다. 동아리 생성·조회와 회원 추가·조회 기능을 유지하고, 직접 연결과 자원 정리를 하던 코드를 Spring JDBC로 변경했습니다.

## 과제 요구사항 반영

| 요구사항 | 구현 |
| --- | --- |
| Spring Initializr 템플릿 다운로드 | 공식 start.spring.io에서 생성한 Java 17 / Gradle Kotlin DSL 템플릿과 Wrapper 사용 |
| 3회차 과제를 Spring으로 이전 | Club·Member·Animal/Dog/Cat 이전, `@Service`·`@Repository`·생성자 주입 적용 |
| JDBC API를 JdbcTemplate으로 변경 | 조회는 `JdbcTemplate.query`와 `RowMapper`, 생성은 동일 JdbcTemplate을 사용하는 `SimpleJdbcInsert` |
| DB 접속 설정 분리 | `application.properties`, `application-postgres.properties`와 환경변수 사용 |
| GitHub 공개 저장소와 주소 제출 | 기존 공개 저장소의 `week04` 폴더로 제공하며, 구글 폼 제출은 사용자가 진행 |

Spring Boot **4.1.2**, Gradle **9.8.2**는 2026-11-10에 Spring Initializr가 생성한 버전입니다. 생성 요청과 템플릿 변경 사항은 [INITIALIZR.md](INITIALIZR.md)에 기록했습니다.

## 빠른 실행

JDK 17 이상을 설치하고 `week04` 폴더에서 실행합니다. 최초 실행은 Gradle 및 Maven 의존성 다운로드를 위한 인터넷 연결이 필요합니다. 전역 Gradle 설치는 필요 없습니다.

```powershell
java -version
.\gradlew.bat test bootJar
.\gradlew.bat bootRun
```

macOS/Linux에서는 대응하는 명령이 `./gradlew test bootJar`, `./gradlew bootRun`입니다. 실행 권한이 없다면 `chmod +x gradlew`를 한 번 실행합니다.

기본 서버 주소는 `http://127.0.0.1:8080`입니다. H2 파일 DB가 `data/week04` 경로에 만들어지며 서버 재시작 후에도 데이터가 유지됩니다. `WRITE_DELAY=0`으로 변경 내용을 즉시 기록합니다. 테스트는 별도의 메모리 DB를 사용합니다. Docker 없이 바로 실행할 수 있습니다.

### 3회차 Main과 같은 콘솔 실습

```powershell
.\gradlew.bat bootRun --args="--demo --spring.main.web-application-type=none"
```

Animal/Dog/Cat 인사 → 동아리 생성·조회 → 가상 회원 추가·조회 순서로 실행한 뒤 종료합니다. 마지막에 `JdbcTemplate practice completed.`가 출력됩니다. 기본 서버 실행에서는 데모 데이터를 자동 추가하지 않습니다.

### PostgreSQL로 실행

3회차에서 사용한 PostgreSQL과 `club`, `member` 테이블을 그대로 사용할 수 있습니다. 다른 DB를 쓰려면 먼저 해당 DB와 접속 계정을 준비합니다. 스키마 초기화는 `CREATE TABLE IF NOT EXISTS`만 실행하므로 기존 테이블과 데이터를 삭제하지 않습니다.

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/postgres'
$env:DB_USER = 'postgres'
$dbPassword = Read-Host 'Local DB password' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $dbPassword).Password
.\gradlew.bat bootRun --args="--spring.profiles.active=postgres"
```

PostgreSQL 프로필에서는 `DB_PASSWORD`가 필수입니다. `.env.example`은 설정 예시이며 자동으로 읽는 파일은 아닙니다. 환경변수는 현재 PowerShell 세션에서만 적용됩니다. 비밀번호를 소스 코드나 Git에 저장하지 않습니다.

포트가 이미 사용 중이면 `$env:PORT = '8081'`을 설정하고 실행합니다.

## API

| 메서드 | 경로 | 동작 | 정상 응답 |
| --- | --- | --- | --- |
| POST | `/clubs` | 동아리 생성 | 201, Location 헤더 |
| GET | `/clubs/{clubId}` | 동아리 조회 | 200 |
| POST | `/clubs/{clubId}/members` | 회원 추가 | 201 |
| GET | `/clubs/{clubId}/members` | 회원 목록 조회 | 200, ID 오름차순 |

회원이 없는 동아리는 `[]`를 반환합니다. 존재하지 않는 동아리는 404, 잘못된 JSON·빈 필수값·잘못된 이메일은 400, 중복 이메일은 409입니다. 3회차 스키마와 같이 이메일은 모든 동아리에 걸쳐 UNIQUE입니다. 회원 응답은 `id`, `name`, `email`만 포함합니다.

서버를 실행한 상태에서 다른 PowerShell 창으로 확인할 수 있습니다.

```powershell
$baseUrl = 'http://127.0.0.1:8080'
$club = Invoke-RestMethod -Method Post -Uri "$baseUrl/clubs" `
    -ContentType 'application/json; charset=utf-8' `
    -Body ([Text.Encoding]::UTF8.GetBytes('{"name":"GDGoC KU"}'))

Invoke-RestMethod -Uri "$baseUrl/clubs/$($club.id)"

$memberBody = @{
    name = '홍길동'
    email = "hong+$($club.id)@example.invalid"
    password = 'example-only'
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$baseUrl/clubs/$($club.id)/members" `
    -ContentType 'application/json; charset=utf-8' `
    -Body ([Text.Encoding]::UTF8.GetBytes($memberBody))

Invoke-RestMethod -Uri "$baseUrl/clubs/$($club.id)/members"
```

`member.password` 컬럼은 3회차 DDL과 실습을 유지하기 위한 학습용 평문 필드입니다. 로그인 기능은 없으며 예제처럼 가상 데이터만 사용합니다. 실제 서비스의 비밀번호 저장 방식을 구현한 과제는 아닙니다.

## 3회차와 달라진 부분

```text
3회차: Main → new ClubService(url, user, password)
             → DriverManager / PreparedStatement / ResultSet → PostgreSQL

4회차: HTTP 요청 → ClubController → ClubService → ClubRepository
                                            → JdbcTemplate / SimpleJdbcInsert
                                            → Spring DataSource(HikariCP) → DB
       --demo → PracticeRunner ────────→ ClubService
```

- Spring Boot가 `DataSource`, 커넥션 풀, `JdbcTemplate`을 설정합니다.
- 생성자로 주입받은 객체를 사용하며 서비스 메서드는 인스턴스 메서드입니다.
- 직접 `DriverManager.getConnection`, `PreparedStatement` 생성, `close`를 하지 않습니다.
- `RowMapper`는 조회된 한 행을 기존 Club·Member 객체로 변환합니다.
- `SimpleJdbcInsert`는 JdbcTemplate을 통해 INSERT와 자동 생성 ID 조회를 수행합니다. PostgreSQL 전용 `RETURNING`에 의존하지 않아 H2에서도 같은 코드를 검증할 수 있습니다.
- `@Transactional`로 생성·회원 추가의 트랜잭션을 관리하고, 조회는 읽기 전용으로 설정했습니다.
- 기존 3회차의 package를 `com.gdgku.week04` 아래로 옮겨 Spring 컴포넌트 검색 범위에 포함했습니다.

## 구조

```text
week04/
  build.gradle.kts, settings.gradle.kts
  gradlew, gradlew.bat, gradle/wrapper/
  src/main/java/com/gdgku/week04/
    Week04Application.java
    PracticeRunner.java
    animal/Animal.java, Dog.java, Cat.java
    club/Club.java, Member.java
    club/ClubController.java, ClubService.java, ClubRepository.java
    club/ApiExceptionHandler.java
  src/main/resources/
    application.properties, application-postgres.properties, schema.sql
  src/test/java/com/gdgku/week04/ClubIntegrationTests.java
  src/test/resources/application-test.properties
```

## 테스트

```powershell
.\gradlew.bat test
```

실제 Spring 서버와 DB를 사용하는 통합 테스트입니다. 생성·조회·회원 분리·정렬, 응답의 비밀번호 제외, 400/404/409, 외래키 제약조건과 SQL 매개변수 바인딩을 확인합니다. 결과는 `build/reports/tests/test/index.html`에서 볼 수 있습니다.

테스트용 `TEST_DB_URL`, `TEST_DB_USER`, `TEST_DB_PASSWORD` 환경변수를 지정하면 같은 테스트를 별도의 PostgreSQL 테스트 DB에서 실행할 수 있습니다. 테스트는 매번 club/member의 모든 행을 지우므로 반드시 **전용 테스트 DB**만 지정해야 합니다. 일반 실행의 `DB_URL`과 기본 H2 파일 DB는 테스트에서 사용하지 않습니다.

## 기존 GitHub 저장소에 넣기

이 `week04` 폴더를 `younghwi-ko/backend-study-junior` 저장소 루트에 추가합니다. `week03` 및 루트의 기존 Spring 프로젝트를 변경할 필요는 없습니다. 실행·테스트는 `week04` 폴더 안에서 수행합니다. 루트 Gradle의 서브프로젝트로 등록하지 않은 독립 프로젝트입니다.

GitHub에 올릴 항목은 이 프로젝트의 소스, 설정, Gradle Wrapper와 문서입니다. `build/`, `.gradle/`, `data/`, `.env`와 로그는 `.gitignore`에서 제외합니다. 제출할 주소는 `https://github.com/younghwi-ko/backend-study-junior/tree/main/week04`입니다. 구글 폼 제출은 사용자가 진행합니다.

## 참고 자료

- [4회차 과제 폼](https://forms.gle/GSqSDcBG8zRvNEr97)
- [4회차 강의](https://youtu.be/mBvmmd6F-oc)
- [4회차 강의 자료](https://docs.google.com/presentation/d/1vF6WEbsYhzTj9TULdzW5Rw3vDtbOvRZ9)
- [3회차 원본 프로젝트](https://github.com/younghwi-ko/backend-study-junior/tree/main/week03)
- [Spring Initializr](https://start.spring.io/)

강의 자료와 기존 3회차 소스를 기준으로 AI의 도움을 받아 작성·검증했습니다. 강의 코드를 문자 단위로 복사한 결과는 아니며 실행 편의를 위한 REST API와 기본 H2 설정을 추가했습니다.
