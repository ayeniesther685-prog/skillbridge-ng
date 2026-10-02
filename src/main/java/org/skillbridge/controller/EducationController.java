package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.EducationRequest;
import org.skillbridge.dto.response.EducationResponse;
import org.skillbridge.service.impl.EducationServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Education")
public class EducationController {

    private final EducationServiceImpl service;

    public EducationController(EducationServiceImpl service) { this.service = service; }

    @GetMapping("/users/{userId}/education")
    public ResponseEntity<List<EducationResponse>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listForUser(userId));
    }

    @PostMapping("/users/me/education")
    public ResponseEntity<EducationResponse> create(@Valid @RequestBody EducationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/education/{id}")
    public ResponseEntity<EducationResponse> update(@PathVariable Long id, @Valid @RequestBody EducationRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/education/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}