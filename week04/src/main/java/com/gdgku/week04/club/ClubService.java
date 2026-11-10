package com.gdgku.week04.club;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClubService {
    private final ClubRepository repository;

    // Main에서 직접 new 하던 서비스를 Spring이 생성하고 의존성을 주입합니다.
    public ClubService(ClubRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Club createClub(String name) {
        return repository.createClub(name);
    }

    public Club getClub(long clubId) {
        return repository.findClub(clubId)
                .orElseThrow(() -> new NoSuchElementException("동아리를 찾을 수 없습니다."));
    }

    @Transactional
    public Member addMember(long clubId, String name, String email, String password) {
        getClub(clubId);
        return repository.addMember(clubId, name, email, password);
    }

    public List<Member> getMembers(long clubId) {
        getClub(clubId);
        return repository.findMembers(clubId);
    }
}
