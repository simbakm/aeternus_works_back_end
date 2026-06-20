package com.example.aeternus_back_end.controller;

import com.example.aeternus_back_end.model.TeamMember;
import com.example.aeternus_back_end.repository.TeamMemberRepository;
import com.example.aeternus_back_end.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamMemberRepository teamRepository;
    private final SupabaseStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<TeamMember>> getAllTeamMembers() {
        return ResponseEntity.ok(teamRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<TeamMember> createTeamMember(@RequestBody TeamMember member) {
        if (member.getImageUrl() != null) {
            member.setImageUrl(fileStorageService.uploadBase64Image(member.getImageUrl()));
        }
        return ResponseEntity.ok(teamRepository.save(member));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeamMember(@PathVariable String id) {
        return teamRepository.findById(id)
                .map(member -> {
                    teamRepository.delete(member);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
