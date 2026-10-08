-- PPT 9페이지 DDL. 새 실습 데이터베이스에 한 번 실행한다.
CREATE TABLE club (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE member (
    id       BIGSERIAL PRIMARY KEY,
    club_id  BIGINT NOT NULL REFERENCES club(id),
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
