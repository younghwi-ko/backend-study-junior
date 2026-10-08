# 검증 기록

원본 과제 검증 날짜: 2026-10-07 (Asia/Seoul)
GitHub week03 정리 및 재검증 날짜: 2026-10-08 (Asia/Seoul)

## GitHub용 복사본 재검증

- 원본 제출 ZIP과 실제 프로젝트 폴더의 17개 파일을 SHA-256으로 비교: 모두 일치. 원본 폴더와 ZIP은 수정하지 않음.
- 기존 Public 저장소를 전체 Git 이력과 함께 복제하고 week03을 독립 폴더로 추가. 기존 src/ 및 Gradle 파일은 변경하지 않음.
- DB 비밀번호를 환경변수 DB_PASSWORD로 옮기고 Main이 ClubService 생성자에 전달하도록 변경. 누락 시 DB 연결 전에 오류가 발생하는 것 확인.
- run.ps1 -SyntaxOnly 및 Java 전체 컴파일 성공. 환경변수 변경 후에도 문법 예제는 DB 비밀번호 없이 실행 가능.
- 기존 PostgreSQL 18 컨테이너를 이용해 week03/run.ps1의 동아리 생성·조회, 회원 추가·조회 성공.
- 실제 출력: Club ID 4, Member ID 4, 이름 홍길동, hong+4@example.invalid 및 JDBC practice completed.
- run.ps1과 setup-db.ps1 PowerShell 구문 검사 통과. GitHub용 setup-db.ps1의 새 DB 생성은 이번 재검증에서 다시 실행하지 않음. 기존 DB 데이터를 보존함.
- 원본 이후 바뀐 이메일과 example-only 비밀번호는 가상 데이터이며 실제 자격증명이 아님.

기존 루트 프로젝트도 컴파일/bootJar 생성, 서버 기동, /hello 및 /users HTTP 200을 확인했습니다. 기존 테스트는 51개 중 42개 통과, 9개 실패했습니다. 기존 소스와 테스트 87개 파일(수정 대상 README/.gitignore 제외)은 바이트 단위로 원본과 일치합니다. week03은 Gradle 소스 경로에 추가하지 않았습니다.

아래는 원본 제출 프로젝트의 검증 기록이며, 당시 출력의 가상 이메일은 GitHub용 복사본의 example.invalid로 바꾸기 전 값입니다.

## 실제 확인한 항목

- Eclipse Adoptium Java/Javac 17 설치 확인.
- `javac -encoding UTF-8 -d out -sourcepath src src/Main.java`: 전체 소스 컴파일 성공.
- `javac -Xlint:all`: 전체 소스 컴파일 성공, 경고 없음.
- `run.ps1 -SyntaxOnly`: 종료 코드 0. 아래 인사 출력 확인.
- JDBC JAR을 공식 Maven 저장소에서 다운로드하고 Java로 읽어 드라이버 버전 42.7.13 확인.
- `ClubService`에 static 선언이 없는 것 확인. Main의 생성자 주입과 네 가지 인스턴스 메서드 호출 확인.
- 별도 임시 소스로 public 클래스 파일 이름 위반, import 누락, 잘못된 @Override의 예상 컴파일 오류 확인. 오류 예제는 제출 프로젝트 소스에 넣지 않음.
- PowerShell 실행 스크립트 구문 검사 성공.
- 제출 ZIP을 별도 폴더에 해제한 뒤 `run.ps1 -SyntaxOnly`로 전체 재컴파일 및 문법 실행 성공. 드라이버와 숨김 VS Code 설정 파일도 ZIP에 포함.

```text
Hello! My name is Buddy and I am 3 years old.
Woof! I am a Golden Retriever.
Hello! My name is Whiskers and I am 2 years old.
Meow! My color is black.
```

JDBC JAR SHA-256:

```text
6E0E4CC2D8CAE902084F8A2B18728B073A6FD9D1F87C9D8BFF8F298C18185B93
```

## Docker 및 JDBC 실제 실행 결과

사용자가 Docker Desktop을 설치한 뒤 다음 항목까지 실제 검증을 완료했습니다.

- Docker 엔진 29.8.2 실행 확인.
- `setup-db.ps1` 실행: 공식 `postgres:18` 이미지 다운로드, `postgres18` 컨테이너 생성, 준비 상태 확인, `CREATE TABLE` 두 번 성공.
- PostgreSQL 서버 버전 18.6 확인. 포트 `127.0.0.1:5432`, DB/계정 `postgres`, 프로젝트 `data` 바인드 마운트 사용.
- `run.ps1` 실행: 문법 예제 및 JDBC 네 메서드 모두 성공, 종료 코드 0.
- Java가 생성한 데이터를 PostgreSQL JOIN 조회로 확인: club ID 1, member ID 1, club_id 1, 이름 홍길동, email hong+1@gdgku.com.
- `sql/practice.sql`을 psql로 실행: 동아리와 회원 INSERT/SELECT 성공, ID 2의 동아리 및 회원 반환, 종료 코드 0.

실제 Java JDBC 출력:

```text
Club: 1 / GDG on Campus KU
Member: 1 / 홍길동 / hong+1@gdgku.com
JDBC practice completed.
```

전체 프로젝트 실행 검증을 완료했습니다. 컨테이너와 실습 데이터는 로컬에 유지했습니다. ZIP에는 소스와 재현에 필요한 파일을 포함하고 DB 저장 파일은 제외했습니다. Google Form 업로드와 제출은 사용자가 진행합니다.
