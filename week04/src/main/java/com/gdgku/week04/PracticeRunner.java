package com.gdgku.week04;

import com.gdgku.week04.animal.Animal;
import com.gdgku.week04.animal.Cat;
import com.gdgku.week04.animal.Dog;
import com.gdgku.week04.club.Club;
import com.gdgku.week04.club.ClubService;
import com.gdgku.week04.club.Member;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PracticeRunner implements ApplicationRunner {
    private final ClubService service;

    public PracticeRunner(ClubService service) {
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        // 3회차 Main의 실습 흐름은 --demo를 지정했을 때만 실행합니다.
        if (!arguments.containsOption("demo")) return;

        Animal[] animals = {
            new Dog("Buddy", 3, "Golden Retriever"), new Cat("Whiskers", 2, "black")
        };
        for (Animal animal : animals) animal.greet();

        Club created = service.createClub("GDG on Campus KU");
        Club found = service.getClub(created.getId());
        System.out.println("Club: " + found.getId() + " / " + found.getName());
        Member added = service.addMember(created.getId(), "홍길동",
                "hong+" + created.getId() + "@example.invalid", "example-only");
        List<Member> members = service.getMembers(created.getId());
        if (members.size() != 1 || members.get(0).getId() != added.getId()) {
            throw new IllegalStateException("Member read did not match inserted member");
        }
        for (Member member : members) {
            System.out.println("Member: " + member.getId() + " / " + member.getName() + " / " + member.getEmail());
        }
        System.out.println("JdbcTemplate practice completed.");
    }
}
