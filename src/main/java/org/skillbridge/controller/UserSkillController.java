package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.skillbridge.dto.response.SkillResponse;
import org.skillbridge.service.impl.UserSkillServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Skills")
public class UserSkillController {

    private final UserSkillServiceImpl service;

    public UserSkillController(UserSkillServiceImpl service) { this.service = service; }

    @GetMapping("/{userId}/skills")
    public ResponseEntity<List<SkillResponse>> listForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listSkillsForUser(userId));
    }

    @PostMapping("/me/skills/{skillId}")
    public ResponseEntity<List<SkillResponse>> add(@PathVariable Long skillId) {
        return ResponseEntity.ok(service.addSkillToCurrentUser(skillId));
    }

    @DeleteMapping("/me/skills/{skillId}")
    public ResponseEntity<List<SkillResponse>> remove(@PathVariable Long skillId) {
        return ResponseEntity.ok(service.removeSkillFromCurrentUser(skillId));
    }
}