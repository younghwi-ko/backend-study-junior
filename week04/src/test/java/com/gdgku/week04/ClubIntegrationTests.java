package com.gdgku.week04;

import com.gdgku.week04.club.Club;
import com.gdgku.week04.club.ClubRepository;
import com.gdgku.week04.club.ClubService;
import com.gdgku.week04.club.Member;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ClubIntegrationTests {
    @LocalServerPort int port;
    @Autowired ClubService service;
    @Autowired ClubRepository repository;
    @Autowired JdbcTemplate jdbcTemplate;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void resetDedicatedTestDatabase() {
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("DELETE FROM club");
    }

    @Test
    void createAndReadClubThroughRealHttpAndDatabase() throws Exception {
        HttpResponse<String> created = request("POST", "/clubs", "{\"name\":\"GDGoC KU\"}");
        assertEquals(201, created.statusCode());
        long id = idOf(created.body());
        assertEquals("/clubs/" + id, created.headers().firstValue("Location").orElseThrow());
        HttpResponse<String> found = request("GET", "/clubs/" + id, null);
        assertEquals(200, found.statusCode());
        assertTrue(found.body().contains("GDGoC KU"));
        assertEquals("GDGoC KU", jdbcTemplate.queryForObject(
                "SELECT name FROM club WHERE id = ?", String.class, id));
    }

    @Test
    void emptyClubReturnsEmptyMemberArray() throws Exception {
        Club club = service.createClub("Empty");
        HttpResponse<String> response = request("GET", "/clubs/" + club.getId() + "/members", null);
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void addAndListMemberWithoutPasswordInResponse() throws Exception {
        Club club = service.createClub("회원 테스트");
        HttpResponse<String> added = request("POST", "/clubs/" + club.getId() + "/members",
                "{\"name\":\"홍길동\",\"email\":\"hong@example.invalid\",\"password\":\"test-only-secret\"}");
        assertEquals(201, added.statusCode());
        long memberId = idOf(added.body());
        HttpResponse<String> listed = request("GET", "/clubs/" + club.getId() + "/members", null);
        assertEquals(200, listed.statusCode());
        assertTrue(listed.body().contains("홍길동"));
        assertEquals(memberId, idOf(listed.body()));
        for (String body : List.of(added.body(), listed.body())) {
            assertFalse(body.contains("password"));
            assertFalse(body.contains("test-only-secret"));
        }
        assertEquals(club.getId(), jdbcTemplate.queryForObject(
                "SELECT club_id FROM member WHERE id = ?", Long.class, memberId));
    }

    @Test
    void membersAreFilteredByClubAndOrderedByGeneratedId() {
        Club first = service.createClub("First");
        Club second = service.createClub("Second");
        Member one = service.addMember(first.getId(), "One", "one@example.invalid", "example-only");
        service.addMember(second.getId(), "Other", "other@example.invalid", "example-only");
        Member two = service.addMember(first.getId(), "Two", "two@example.invalid", "example-only");
        assertEquals(List.of(one.getId(), two.getId()), service.getMembers(first.getId())
                .stream().map(Member::getId).toList());
    }

    @Test
    void missingClubReturns404ForReadListAndInsert() throws Exception {
        assertEquals(404, request("GET", "/clubs/99999999", null).statusCode());
        assertEquals(404, request("GET", "/clubs/99999999/members", null).statusCode());
        assertEquals(404, request("POST", "/clubs/99999999/members",
                "{\"name\":\"Ghost\",\"email\":\"ghost@example.invalid\",\"password\":\"example-only\"}").statusCode());
        assertEquals(0, count("member"));
    }

    @Test
    void invalidRequestsReturn400AndDoNotInsertData() throws Exception {
        assertEquals(400, request("POST", "/clubs", "{\"name\":\" \"}").statusCode());
        assertEquals(400, request("POST", "/clubs", "{\"name\":\"" + "a".repeat(101) + "\"}").statusCode());
        assertEquals(400, request("POST", "/clubs", "{").statusCode());
        assertEquals(400, request("GET", "/clubs/not-a-number", null).statusCode());
        assertEquals(0, count("club"));
        Club club = service.createClub("Valid");
        assertEquals(400, request("POST", "/clubs/" + club.getId() + "/members",
                "{\"name\":\"Name\",\"email\":\"invalid-email\",\"password\":\"example-only\"}").statusCode());
        assertEquals(400, request("POST", "/clubs/" + club.getId() + "/members",
                "{\"name\":\"Name\",\"email\":\"name@example.invalid\",\"password\":\" \"}").statusCode());
        assertEquals(0, count("member"));
    }

    @Test
    void duplicateEmailReturns409AndPreservesOriginalMember() throws Exception {
        Club first = service.createClub("First");
        Club second = service.createClub("Second");
        Member original = service.addMember(first.getId(), "Original", "same@example.invalid", "example-only");
        HttpResponse<String> duplicate = request("POST", "/clubs/" + second.getId() + "/members",
                "{\"name\":\"Duplicate\",\"email\":\"same@example.invalid\",\"password\":\"private-test-value\"}");
        assertEquals(409, duplicate.statusCode());
        assertFalse(duplicate.body().contains("private-test-value"));
        assertFalse(duplicate.body().contains("INSERT"));
        assertFalse(duplicate.body().contains("jdbc:"));
        assertEquals(1, count("member"));
        assertEquals(original.getId(), service.getMembers(first.getId()).get(0).getId());
        assertTrue(service.getMembers(second.getId()).isEmpty());
    }

    @Test
    void databaseForeignKeyRejectsOrphanMember() {
        assertThrows(DataIntegrityViolationException.class, () -> repository.addMember(
                99999999L, "Orphan", "orphan@example.invalid", "example-only"));
        assertEquals(0, count("member"));
    }

    @Test
    void sqlLookingInputIsStoredAsData() {
        String name = "O'Reilly'); DROP TABLE club; --";
        Club club = service.createClub(name);
        assertEquals(name, service.getClub(club.getId()).getName());
        Club next = service.createClub("Still present");
        assertTrue(next.getId() > club.getId());
        assertEquals(2, count("club"));
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private long idOf(String json) {
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(json);
        assertTrue(matcher.find(), () -> "No id in response: " + json);
        return Long.parseLong(matcher.group(1));
    }

    private int count(String table) {
        // table은 테스트 내부의 club/member 상수만 전달합니다.
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }
}
