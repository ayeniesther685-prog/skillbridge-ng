package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ChallengeSubmitRequest;
import org.skillbridge.dto.response.ChallengeResponse;
import org.skillbridge.dto.response.ChallengeSubmissionResponse;
import org.skillbridge.service.impl.ChallengeServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@Tag(name = "Challenges")
public class ChallengeController {

    private final ChallengeServiceImpl service;

    public ChallengeController(ChallengeServiceImpl service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<ChallengeResponse>> list() {
        return ResponseEntity.ok(service.listActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ChallengeSubmissionResponse> submit(
            @PathVariable Long id,
            @Valid @RequestBody ChallengeSubmitRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(id, req));
    }

    @GetMapping("/me/submissions")
    public ResponseEntity<List<ChallengeSubmissionResponse>> mine() {
        return ResponseEntity.ok(service.mySubmissions());
    }
}