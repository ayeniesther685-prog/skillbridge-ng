package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ProviderVerificationSubmitRequest;
import org.skillbridge.dto.response.ProviderVerificationResponse;
import org.skillbridge.dto.response.ProviderVerificationStatusResponse;
import org.skillbridge.service.ProviderVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
@Tag(name = "Provider Verification")
public class ProviderVerificationController {

    private final ProviderVerificationService service;

    public ProviderVerificationController(ProviderVerificationService service) {
        this.service = service;
    }

    @GetMapping("/me/verification")
    public ResponseEntity<ProviderVerificationResponse> getMine() {
        return ResponseEntity.ok(service.getMine());
    }

    @PostMapping("/me/verification/submit")
    public ResponseEntity<ProviderVerificationResponse> submit(
            @Valid @RequestBody ProviderVerificationSubmitRequest req) {
        return ResponseEntity.ok(service.submit(req));
    }

    @GetMapping("/{userId}/verification-status")
    public ResponseEntity<ProviderVerificationStatusResponse> status(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getStatusForUser(userId));
    }
}