package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.CertificationRequest;
import org.skillbridge.dto.response.CertificationResponse;
import org.skillbridge.service.impl.CertificationServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Certifications")
public class CertificationController {

    private final CertificationServiceImpl service;

    public CertificationController(CertificationServiceImpl service) { this.service = service; }

    @GetMapping("/users/{userId}/certifications")
    public ResponseEntity<List<CertificationResponse>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listForUser(userId));
    }

    @PostMapping("/users/me/certifications")
    public ResponseEntity<CertificationResponse> create(@Valid @RequestBody CertificationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/certifications/{id}")
    public ResponseEntity<CertificationResponse> update(@PathVariable Long id, @Valid @RequestBody CertificationRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/certifications/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}