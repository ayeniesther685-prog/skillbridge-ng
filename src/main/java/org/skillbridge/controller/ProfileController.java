package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ProfileRequest;
import org.skillbridge.dto.response.ProfileResponse;
import org.skillbridge.service.impl.ProfileServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Profile")
public class ProfileController {

    private final ProfileServiceImpl service;

    public ProfileController(ProfileServiceImpl service) { this.service = service; }

    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<ProfileResponse> getForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getForUser(userId));
    }

    @GetMapping("/users/me/profile")
    public ResponseEntity<ProfileResponse> getMine() {
        return ResponseEntity.ok(service.getForCurrentUser());
    }

    @PutMapping("/users/me/profile")
    public ResponseEntity<ProfileResponse> upsertMine(@Valid @RequestBody ProfileRequest req) {
        return ResponseEntity.ok(service.upsertForCurrentUser(req));
    }
}