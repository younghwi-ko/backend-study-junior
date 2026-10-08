import animal.Animal;
import animal.Cat;
import animal.Dog;
import club.Club;
import club.ClubService;
import club.Member;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        Animal[] animals = {
            new Dog("Buddy", 3, "Golden Retriever"),
            new Cat("Whiskers", 2, "black")
        };
        for (Animal animal : animals) animal.greet();

        // DB 설치 전에도 문법 실습을 따로 실행할 수 있다.
        if (args.length == 1 && args[0].equals("--syntax")) return;
        if (args.length != 0) throw new IllegalArgumentException("Usage: Main [--syntax]");

        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
        String user = System.getenv().getOrDefault("DB_USER", "postgres");
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Set the DB_PASSWORD environment variable before JDBC practice.");
        }
        ClubService clubService = new ClubService(url, user, password);

        Club created = clubService.createClub("GDG on Campus KU");
        Club found = clubService.getClub(created.getId());
        if (found == null || !found.getName().equals(created.getName())) {
            throw new IllegalStateException("Club read did not match inserted club");
        }
        System.out.println("Club: " + found.getId() + " / " + found.getName());

        // UNIQUE email 충돌 없이 실습을 반복하도록 생성된 club ID를 사용한다.
        Member added = clubService.addMember(created.getId(), "홍길동",
                "hong+" + created.getId() + "@example.invalid", "example-only");
        List<Member> members = clubService.getMembers(created.getId());
        if (members.size() != 1 || members.get(0).getId() != added.getId()) {
            throw new IllegalStateException("Member read did not match inserted member");
        }
        for (Member member : members) {
            System.out.println("Member: " + member.getId() + " / " + member.getName() + " / " + member.getEmail());
        }
        System.out.println("JDBC practice completed.");
    }
}
