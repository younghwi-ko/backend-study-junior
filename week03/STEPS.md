# 단계별 작업 내용

## 1. 빈 폴더에서 Java 프로젝트 생성

강의 5:03부터 진행하는 순수 Java 실습을 기준으로 `backend-study-java-lang`과 `src/Main.java`를 만들었습니다. `public static void main(String[] args)`가 실행 진입점입니다. 빌드 도구 대신 강의의 `javac -d out -sourcepath src src/Main.java`를 사용합니다. 한글 소스에 맞춰 `-encoding UTF-8`을 추가했습니다.

## 2. Java 문법: 상속 → 파일 분리 → package/import

강의 8:50–34:31의 `Animal`, `Dog`, `Cat` 예제를 재현했습니다. `Animal`은 protected 이름·나이 필드와 생성자·greet 메서드를 갖습니다. `Dog`는 품종, `Cat`은 색깔을 추가하며 `extends`, `super`, `@Override`를 사용합니다. Main의 Animal 배열에서 greet를 호출하면 실제 객체의 재정의 메서드가 실행됩니다.

처음 한 파일에 작성하는 대신 제출본은 강의에서 정리한 최종 상태입니다. 각 public 클래스를 같은 이름의 파일에 나누고 `src/animal`로 이동한 뒤 각 파일에 `package animal;`을 선언했습니다. `Main.java`에는 `import animal.Animal;`, `import animal.Dog;`, `import animal.Cat;`을 추가했습니다.

문법 실습 중간 단계는 아래처럼 재현할 수 있습니다. 제출 소스는 모두 정상 컴파일되는 최종 상태로 유지합니다.

| 실습 변경 | 컴파일 결과 또는 해결 방법 |
| --- | --- |
| `Main.java`에 `public class Dog`도 작성 | public 클래스는 `Dog.java`에 있어야 한다는 오류. public을 임시로 떼거나 파일 분리 |
| Main에서 `import animal.Dog`를 제거하고 `new Dog(...)` 사용 | cannot find symbol. `new animal.Dog(...)`로 풀네임 사용 후 import로 정리 |
| Dog의 package를 다른 이름으로 변경 | 소스 경로와 package가 달라져 Main의 import 실패. `package animal;`로 복원 |
| 부모 greet 이름과 다르게 `@Override` 메서드 작성 | does not override 오류. 메서드 이름·매개변수를 부모와 일치시킴 |
| 다른 패키지인 Main에서 `dog.name` 접근 | protected 접근 오류. 상속받은 Dog 내부에서는 접근 가능 |
| Animal 클래스의 public 제거 | 다른 패키지에서 클래스 자체를 사용할 수 없음 |

default는 같은 패키지에서, protected는 같은 패키지 및 다른 패키지의 하위 클래스에서 정해진 상속 규칙에 따라, public은 외부 패키지에서도 접근 가능합니다. Dog/Cat은 부모의 protected 필드를 상속받습니다. 최종 예제는 인사 출력에 부모 메서드를 재사용합니다.

실행: `run.ps1 -SyntaxOnly`. DB 설치 전에도 이 부분은 실행할 수 있습니다.

## 3. Docker PostgreSQL 설치

강의 37:40 및 PPT 7–8페이지의 PostgreSQL 18, 컨테이너 이름 `postgres18`, DB/계정 `postgres`, 호스트 포트 5432, `data` 바인드 마운트를 사용합니다. GitHub용 복사본의 비밀번호는 DB_PASSWORD 환경변수에서 읽습니다. `setup-db.ps1`에 컨테이너 생성 → 준비 상태 대기 → DDL 실행 순서를 넣었습니다. Docker Desktop 설치는 사용자가 직접 진행했습니다.

## 4. DDL로 club/member 생성

강의 58:10 및 PPT 9페이지의 DDL을 `sql/schema.sql`에 저장했습니다. `club`은 id와 name, `member`는 id·club_id·name·email·password입니다. BIGSERIAL 기본키, NOT NULL, email UNIQUE, `member.club_id → club.id` 외래키를 그대로 사용합니다.

PPT 11페이지의 동아리 INSERT/SELECT 및 회원 INSERT/SELECT도 `sql/practice.sql`에 저장했습니다. 강의의 고정 ID 1 대신 `RETURNING`으로 실제 ID를 전달하여 새 DB와 반복 실습 모두에서 동작합니다.

## 5. JDBC로 Java와 PostgreSQL 연결

강의 1:25:35부터의 `lib` 폴더와 PPT 13페이지의 `postgresql-42.7.13.jar`를 포함했습니다. `src/club`에 데이터 클래스 `Club`, `Member`와 SQL 담당 `ClubService`를 나누었습니다. 강의 2:17:23 이후의 최종 package 이름 `club`을 사용합니다.

`ClubService`에는 강의/PPT의 네 메서드만 구현했습니다: `createClub`, `getClub`, `addMember`, `getMembers`. DriverManager로 Connection을 열고 PreparedStatement의 `?`에 값을 전달한 뒤 ResultSet을 데이터 객체로 변환합니다. 모든 Connection/Statement/ResultSet은 try-with-resources로 닫습니다. UPDATE/DELETE, REST API, Spring 서버는 추가하지 않았습니다.

Windows classpath는 `java -cp "out;lib/*" Main`입니다. javac 단계는 JDK의 java.sql 타입만 사용하므로 드라이버가 필요하지 않지만 실행 단계에는 드라이버가 필요합니다. VS Code 설정에도 `lib/**/*.jar`를 등록했습니다.

## 6. static 제거와 의존성 관계

강의 2:42:18–2:49:23을 최종 코드에 반영했습니다.

제거 전 개념:

```java
// 접속 정보와 메서드가 모두 ClubService 클래스에 고정
private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
public static Club createClub(String name) { /* JDBC */ }
// Main: ClubService.createClub("GDG on Campus KU");
```

제거 후 GitHub용 코드의 핵심:

```java
// ClubService: static 없는 인스턴스 필드와 생성자
private final String url;
private final String user;
private final String password;
public ClubService(String url, String user, String password) {
    this.url = url;
    this.user = user;
    this.password = password;
}
// Main의 main 내부: 환경변수를 읽어 생성자로 전달
String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
String user = System.getenv().getOrDefault("DB_USER", "postgres");
String password = System.getenv("DB_PASSWORD");
// 실제 코드는 password가 비어 있으면 여기서 오류를 발생시킨다.
ClubService clubService = new ClubService(url, user, password);
clubService.createClub("GDG on Campus KU");
```

`Main → ClubService 인스턴스 → JDBC → PostgreSQL` 순서로 의존합니다. 서비스는 Main이 제공한 접속 정보를 사용합니다. 서비스 내부의 static 필드·메서드는 모두 제거했습니다. 진입점 `main`은 static을 유지합니다. 원본의 Main 설정 상수와 static 서비스 참조는 GitHub용 복사본에서 main 내부의 환경변수 처리와 지역 인스턴스로 바꿨습니다. credentials.json은 추가하지 않았습니다.

## 7. 원본 제출 및 GitHub용 복사본

과제 제출 당시에는 프로젝트 소스와 JDBC JAR, DDL/DML, 실행 스크립트, 설명과 검증 기록을 ZIP으로 압축했습니다. 기존 제출 폴더와 ZIP은 보존하고 이 별도 복사본을 기존 Public 저장소의 week03 폴더에 추가하도록 정리했습니다. DB 비밀번호는 환경변수로 옮기고 가상 이메일은 example.invalid 도메인으로 바꿨습니다. DB 데이터, 빌드 산출물, 실제 환경변수 파일, 제출 ZIP은 Git에서 제외합니다.

## 재현 범위

강의 자동 생성 자막과 PPT를 확인해 패키지 구조·DB 설정·테이블·서비스 메서드·생성자 주입 방식을 맞췄습니다. 화면에 등장한 모든 코드의 문자 단위 복제는 아닙니다. 문법 예제 문구와 실행 보조 스크립트, 자원 정리, 반복 실행 email 처리 및 조회 결과 확인은 실행 가능하게 구성했습니다. 앞 회차 Spring 프로젝트는 설명용 비교 대상이므로 이번 제출물에 추가하지 않았습니다.
