package com.gdgku.week04.club;

// 3회차처럼 응답 모델에는 비밀번호를 보관하지 않습니다.
public class Member {
    private final long id;
    private final String name;
    private final String email;

    public Member(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
