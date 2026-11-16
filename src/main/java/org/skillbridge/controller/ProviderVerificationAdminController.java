package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.AdminVerificationDecisionRequest;
import org.skillbridge.dto.response.ProviderVerificationEventResponse;
import org.skillbridge.dto.response.ProviderVerificationResponse;
import org.skillbridge.enums.ProviderType;
import org.skillbridge.enums.VerificationStatus;
import org.skillbridge.service.ProviderVerificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/provider-verifications")
@Tag(name = "Provider Verification (Admin)")
public class ProviderVerificationAdminController {

    private final ProviderVerificationService service;

    public ProviderVerificationAdminController(ProviderVerificationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<ProviderVerificationResponse>> list(
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(required = false) ProviderType type,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.adminList(status, type, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderVerificationResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.adminGet(id));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<ProviderVerificationEventResponse>> events(@PathVariable Long id) {
        return ResponseEntity.ok(service.adminEvents(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ProviderVerificationResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody AdminVerificationDecisionRequest req) {
        return ResponseEntity.ok(service.adminApprove(id, req.reason()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ProviderVerificationResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody AdminVerificationDecisionRequest req) {
        return ResponseEntity.ok(service.adminReject(id, req.reason()));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<ProviderVerificationResponse> suspend(
            @PathVariable Long id,
            @Valid @RequestBody AdminVerificationDecisionRequest req) {
        return ResponseEntity.ok(service.adminSuspend(id, req.reason()));
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<ProviderVerificationResponse> revoke(
            @PathVariable Long id,
            @Valid @RequestBody AdminVerificationDecisionRequest req) {
        return ResponseEntity.ok(service.adminRevoke(id, req.reason()));
    }
}