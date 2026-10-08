-- PPT 11페이지 DML. psql의 \gset으로 실제 생성된 ID를 사용한다.
INSERT INTO club (name) VALUES ('GDG on Campus KU')
RETURNING id AS club_id \gset

SELECT id, name FROM club WHERE id = :club_id;

INSERT INTO member (club_id, name, email, password)
VALUES (:club_id, '홍길동', 'hong+' || :club_id || '@example.invalid', 'example-only')
RETURNING id, name, email;

SELECT id, name, email FROM member WHERE club_id = :club_id;
