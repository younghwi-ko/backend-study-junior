package club;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClubService {
    private final String url;
    private final String user;
    private final String password;

    // 강의 마지막 실습: 접속 정보를 Main에서 생성자로 주입한다.
    public ClubService(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public Club createClub(String name) throws SQLException {
        String sql = "INSERT INTO club (name) VALUES (?) RETURNING id, name";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Club insert returned no row");
                return new Club(result.getLong("id"), result.getString("name"));
            }
        }
    }

    public Club getClub(long clubId) throws SQLException {
        String sql = "SELECT id, name FROM club WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, clubId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? new Club(result.getLong("id"), result.getString("name")) : null;
            }
        }
    }

    public Member addMember(long clubId, String name, String email, String memberPassword)
            throws SQLException {
        String sql = "INSERT INTO member (club_id, name, email, password) VALUES (?, ?, ?, ?) "
                + "RETURNING id, name, email";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, clubId);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, memberPassword);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Member insert returned no row");
                return new Member(result.getLong("id"), result.getString("name"), result.getString("email"));
            }
        }
    }

    public List<Member> getMembers(long clubId) throws SQLException {
        String sql = "SELECT id, name, email FROM member WHERE club_id = ? ORDER BY id";
        List<Member> members = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, clubId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    members.add(new Member(result.getLong("id"), result.getString("name"), result.getString("email")));
                }
            }
        }
        return members;
    }
}
