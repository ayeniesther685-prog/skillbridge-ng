package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ApplicationCreateRequest;
import org.skillbridge.dto.request.ApplicationStatusUpdateRequest;
import org.skillbridge.dto.response.ApplicationResponse;
import org.skillbridge.service.impl.ApplicationServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationServiceImpl service;

    public ApplicationController(ApplicationServiceImpl service) { this.service = service; }

    @PostMapping("/jobs/{jobId}/applications")
    public ResponseEntity<ApplicationResponse> apply(@PathVariable Long jobId,
                                                     @Valid @RequestBody ApplicationCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.apply(jobId, req));
    }

    @GetMapping("/applications")
    public ResponseEntity<Page<ApplicationResponse>> mine(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listMine(pageable));
    }

    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<Page<ApplicationResponse>> forJob(
            @PathVariable Long jobId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listForJob(jobId, pageable));
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<ApplicationResponse> changeStatus(@PathVariable Long id,
                                                            @Valid @RequestBody ApplicationStatusUpdateRequest req) {
        return ResponseEntity.ok(service.changeStatus(id, req.status()));
    }

    @PutMapping("/applications/{id}/withdraw")
    public ResponseEntity<ApplicationResponse> withdraw(@PathVariable Long id) {
        return ResponseEntity.ok(service.withdraw(id));
    }

    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}