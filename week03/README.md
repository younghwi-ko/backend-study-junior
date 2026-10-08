# GDGoC KU 5기 branch Junior 3회차 실습 (week03)

GDGoC KU branch:BE 주니어 스터디 강의 실습을 기반으로 작성한 순수 Java 프로젝트입니다. Java의 상속·package·import를 연습하고, Docker의 PostgreSQL에 JDBC로 연결하여 동아리와 회원을 생성·조회합니다.

최종 제출 프로젝트를 보존한 별도 GitHub용 복사본입니다. 기존 저장소의 `week03` 폴더에 넣은 독립 프로젝트이며, 루트 Gradle 설정과 기존 소스는 그대로 유지합니다. 접속 비밀번호를 환경변수로 옮겼으며, 강의의 서비스 생성자 주입 방식과 SQL 구조를 유지했습니다.

## 사용 기술

- Java 17 이상: 클래스, 상속, 생성자, 메서드 재정의, package/import
- PostgreSQL 18: 기본키·외래키·NOT NULL·UNIQUE와 INSERT/SELECT
- Docker Desktop의 Linux containers: 로컬 PostgreSQL 실행
- PostgreSQL JDBC 42.7.13: DriverManager, PreparedStatement, ResultSet
- PowerShell: 컴파일과 실행 보조 스크립트

Maven, Gradle, Spring 서버는 사용하지 않습니다. 기능은 동아리 생성·조회, 회원 추가·조회 네 가지입니다.

## 프로젝트 구조

```text
week03/
  src/
    Main.java
    animal/Animal.java, Dog.java, Cat.java
    club/Club.java, Member.java, ClubService.java
  lib/postgresql-42.7.13.jar
  sql/schema.sql
  sql/practice.sql
  .vscode/settings.json
  .env.example
  .gitignore
  .gitattributes
  run.ps1
  setup-db.ps1
  README.md
  STEPS.md
  VERIFICATION.md
```

`out/`와 `data/`는 실행 시 생성되며 Git에 포함하지 않습니다. 강의에서 사용하는 JDBC JAR은 별도 다운로드 없이 실행할 수 있도록 포함했습니다.

## 실행 방법 (Windows PowerShell)

Java JDK와 Docker Desktop을 설치합니다. Docker Desktop의 엔진을 실행하고 프로젝트 폴더에서 아래 명령을 사용합니다. 환경변수는 현재 PowerShell 세션에만 적용됩니다. `.env.example`은 참고용이며 스크립트가 자동으로 읽지는 않습니다.

```powershell
java -version
javac -version
docker info

# DB 없이 전체 소스를 컴파일하고 Java 문법 예제를 실행
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -SyntaxOnly

$env:DB_URL = 'jdbc:postgresql://localhost:5432/postgres'
$env:DB_USER = 'postgres'

# 비밀번호를 파일이나 명령 기록에 직접 쓰지 않고 입력
$secureDbPassword = Read-Host 'Local DB password' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secureDbPassword).Password

# 빈 환경에서 최초 한 번: 컨테이너와 club/member 테이블 생성
powershell -NoProfile -ExecutionPolicy Bypass -File .\setup-db.ps1

# JDBC 동아리 생성/조회 및 회원 추가/조회
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1
```

`setup-db.ps1`은 `postgres18` 컨테이너에 PostgreSQL 18을 실행하고, `127.0.0.1:5432`에만 포트를 엽니다. 데이터는 프로젝트의 `data` 폴더를 `/var/lib/postgresql`에 마운트해 저장합니다. Docker의 초기 비밀번호와 Java의 접속 비밀번호는 같은 DB_PASSWORD를 사용합니다. 초기 DB와 계정은 `postgres`입니다.

기존 과제의 `postgres18` 컨테이너가 있으면 **setup-db.ps1을 다시 실행할 필요가 없습니다**. `docker start postgres18`로 시작하고, 기존 컨테이너에서 설정한 비밀번호를 위 입력 단계에서 사용한 뒤 `run.ps1`을 실행합니다. 환경변수를 변경해도 이미 생성된 DB의 비밀번호가 변경되지는 않습니다. 스크립트는 기존 컨테이너가 있으면 중단하고 데이터를 덮어쓰지 않습니다.

직접 컴파일·실행하는 방법:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -encoding UTF-8 -d out -sourcepath src src/Main.java
java -Dfile.encoding=UTF-8 -cp "out;lib/*" Main
```

DB_URL과 DB_USER는 생략하면 각각 `jdbc:postgresql://localhost:5432/postgres`, `postgres`를 사용합니다. DB_PASSWORD는 기본값 없이 필수입니다. 비밀번호 없이 `--syntax` 모드는 실행할 수 있습니다.

## SQL 실습

`schema.sql`의 DDL은 빈 DB에 한 번만 적용합니다. setup-db.ps1이 이미 적용했다면 다시 실행하지 않습니다.

```powershell
Get-Content -Raw .\sql\schema.sql -Encoding UTF8 | docker exec -i postgres18 psql -U postgres -d postgres -v ON_ERROR_STOP=1
Get-Content -Raw .\sql\practice.sql -Encoding UTF8 | docker exec -i postgres18 psql -U postgres -d postgres -v ON_ERROR_STOP=1
docker exec postgres18 psql -U postgres -d postgres -c "SELECT id, name FROM club ORDER BY id;"
docker exec postgres18 psql -U postgres -d postgres -c "SELECT id, club_id, name, email FROM member ORDER BY id;"
```

Java 실행은 동아리와 회원을 새로 추가하고 조회 결과를 확인합니다. ID는 DB에서 생성하며 실행마다 달라집니다. 회원 email에 생성한 동아리 ID를 붙여 반복 실습의 UNIQUE 충돌을 방지합니다. 마지막 줄에 `JDBC practice completed.`가 표시됩니다.

회원 이름 `홍길동`, `@example.invalid` 주소와 `example-only` 문자열은 가상 실습 데이터입니다. member.password 컬럼은 강의 DDL 재현을 위한 것으로 로그인 기능이나 비밀번호 해시 기능은 구현되어 있지 않습니다. 실제 사용자 데이터를 넣지 않는 로컬 학습 예제입니다.

## 학습한 내용

- public 클래스별 파일 분리와 폴더/package 이름 대응
- import, 접근제어자, 상속, super, @Override와 다형성
- DDL을 통한 club/member 테이블 및 외래키 관계 정의
- JDBC의 연결·매개변수 바인딩·결과 객체 변환·자원 정리
- ClubService의 static 제거와 생성자를 통한 접속 정보 전달

의존성은 `Main → ClubService 인스턴스 → JDBC → PostgreSQL` 순서입니다. Main이 환경변수를 읽어 ClubService 생성자에 전달하며, 서비스 내부에는 static 선언이 없습니다. 자세한 단계는 [STEPS.md](STEPS.md), 검증 결과는 [VERIFICATION.md](VERIFICATION.md)를 참고하세요.

## 로컬 설정과 Git

실제 접속 비밀번호, `.env`, DB 데이터, class 파일, 로그, 제출 ZIP을 커밋하지 않습니다. `.env.example`의 비밀번호 항목은 교체용 표식입니다. 기존 `backend-study-junior` 저장소는 Public이므로 실제 자격증명이 Git 기록에 들어가지 않도록 코드와 업로드 파일을 검사했습니다.

접속 거절은 Docker 엔진과 5432 포트, 테이블 없음은 DDL 적용, `No suitable driver`는 `lib` 및 classpath를 확인하세요. 포트를 바꾸려면 Docker 호스트 포트와 DB_URL을 함께 맞춥니다.

## 강의 자료

- [GDGoC KU Branch BE Junior 3차시 강의](https://www.youtube.com/watch?v=0IRndJJyQL4)
- [강의 PPT](https://docs.google.com/presentation/d/119wp4B6HYOvqzaRB5Jv8e_bekQ5cjWZ3/edit)
- [강의 지정 PostgreSQL JDBC 42.7.13](https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.13/postgresql-42.7.13.jar)

강의 자막과 PPT를 참고해 패키지·테이블·서비스 구조를 재현했습니다. 사용자는 Docker 설치·초기 설정과 과제 제출을 직접 진행했고, 코드 작성·실행 검증·GitHub 정리는 AI의 도움으로 진행했습니다. 강의 코드를 문자 단위로 복제한 결과는 아니며, 실행 보조와 GitHub 업로드를 위한 환경변수 처리를 추가했습니다.
