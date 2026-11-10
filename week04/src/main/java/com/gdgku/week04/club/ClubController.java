package com.gdgku.week04.club;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clubs")
public class ClubController {
    private final ClubService service;

    public ClubController(ClubService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Club> createClub(@Valid @RequestBody CreateClubRequest request) {
        Club club = service.createClub(request.name());
        return ResponseEntity.created(URI.create("/clubs/" + club.getId())).body(club);
    }

    @GetMapping("/{clubId}")
    public Club getClub(@PathVariable long clubId) {
        return service.getClub(clubId);
    }

    @PostMapping("/{clubId}/members")
    public ResponseEntity<Member> addMember(
            @PathVariable long clubId, @Valid @RequestBody AddMemberRequest request) {
        Member member = service.addMember(clubId, request.name(), request.email(), request.password());
        return ResponseEntity.status(201).body(member);
    }

    @GetMapping("/{clubId}/members")
    public List<Member> getMembers(@PathVariable long clubId) {
        return service.getMembers(clubId);
    }

    public record CreateClubRequest(@NotBlank @Size(max = 100) String name) {}

    public record AddMemberRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 255) String password) {}
}
