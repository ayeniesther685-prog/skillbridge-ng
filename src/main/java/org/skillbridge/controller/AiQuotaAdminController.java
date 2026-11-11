package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ChangeTierRequest;
import org.skillbridge.dto.request.GrantCreditsRequest;
import org.skillbridge.dto.response.AiQuotaStatusResponse;
import org.skillbridge.service.impl.AiQuotaServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ai")
@Tag(name = "AI Quota Admin")
public class AiQuotaAdminController {

    private final AiQuotaServiceImpl service;

    public AiQuotaAdminController(AiQuotaServiceImpl service) { this.service = service; }

    @PostMapping("/quota/{userId}/grant")
    public ResponseEntity<AiQuotaStatusResponse> grant(
            @PathVariable Long userId,
            @Valid @RequestBody GrantCreditsRequest req) {
        return ResponseEntity.ok(service.grantCredits(userId, req.amount()));
    }

    @PutMapping("/quota/{userId}/tier")
    public ResponseEntity<AiQuotaStatusResponse> changeTier(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeTierRequest req) {
        return ResponseEntity.ok(service.changeTier(userId, req.tierName()));
    }
}