package com.gdgku.week04.club;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class ClubRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert clubInsert;
    private final SimpleJdbcInsert memberInsert;

    public ClubRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // SimpleJdbcInsert도 주입받은 JdbcTemplate과 동일한 DataSource를 사용합니다.
        this.clubInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("club").usingColumns("name").usingGeneratedKeyColumns("id");
        this.memberInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member").usingColumns("club_id", "name", "email", "password")
                .usingGeneratedKeyColumns("id");
    }

    public Club createClub(String name) {
        long id = clubInsert.executeAndReturnKey(Map.of("name", name)).longValue();
        return new Club(id, name);
    }

    public Optional<Club> findClub(long clubId) {
        List<Club> clubs = jdbcTemplate.query(
                "SELECT id, name FROM club WHERE id = ?", clubMapper(), clubId);
        return clubs.stream().findFirst();
    }

    public Member addMember(long clubId, String name, String email, String password) {
        long id = memberInsert.executeAndReturnKey(Map.of(
                "club_id", clubId, "name", name, "email", email, "password", password)).longValue();
        return new Member(id, name, email);
    }

    public List<Member> findMembers(long clubId) {
        return jdbcTemplate.query(
                "SELECT id, name, email FROM member WHERE club_id = ? ORDER BY id",
                memberMapper(), clubId);
    }

    private RowMapper<Club> clubMapper() {
        return (rs, rowNum) -> new Club(rs.getLong("id"), rs.getString("name"));
    }

    private RowMapper<Member> memberMapper() {
        return (rs, rowNum) -> new Member(
                rs.getLong("id"), rs.getString("name"), rs.getString("email"));
    }
}
