package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.WorkExperienceRequest;
import org.skillbridge.dto.response.WorkExperienceResponse;
import org.skillbridge.service.impl.WorkExperienceServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Work Experience")
public class WorkExperienceController {

    private final WorkExperienceServiceImpl service;

    public WorkExperienceController(WorkExperienceServiceImpl service) { this.service = service; }

    @GetMapping("/users/{userId}/experience")
    public ResponseEntity<List<WorkExperienceResponse>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listForUser(userId));
    }

    @PostMapping("/users/me/experience")
    public ResponseEntity<WorkExperienceResponse> create(@Valid @RequestBody WorkExperienceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/experience/{id}")
    public ResponseEntity<WorkExperienceResponse> update(@PathVariable Long id, @Valid @RequestBody WorkExperienceRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}