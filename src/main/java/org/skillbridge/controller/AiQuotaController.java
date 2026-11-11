package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ChangeTierRequest;
import org.skillbridge.dto.request.GrantCreditsRequest;
import org.skillbridge.dto.response.AiQuotaStatusResponse;
import org.skillbridge.dto.response.AiQuotaTierResponse;
import org.skillbridge.dto.response.AiUsageEventResponse;
import org.skillbridge.service.impl.AiQuotaServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "AI Quota")
public class AiQuotaController {

    private final AiQuotaServiceImpl service;

    public AiQuotaController(AiQuotaServiceImpl service) { this.service = service; }

    @GetMapping("/ai/quota")
    public ResponseEntity<AiQuotaStatusResponse> myStatus() {
        return ResponseEntity.ok(service.getMyStatus());
    }

    @GetMapping("/ai/usage")
    public ResponseEntity<Page<AiUsageEventResponse>> myUsage(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listMyUsage(pageable));
    }

    @GetMapping("/ai/tiers")
    public ResponseEntity<List<AiQuotaTierResponse>> tiers() {
        return ResponseEntity.ok(service.listTiers());
    }
}