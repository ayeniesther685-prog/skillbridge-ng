package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.DisputeMessageCreateRequest;
import org.skillbridge.dto.request.DisputeResolutionRequest;
import org.skillbridge.dto.response.DisputeResponse;
import org.skillbridge.dto.response.DisputeSummaryResponse;
import org.skillbridge.enums.DisputeStatus;
import org.skillbridge.service.DisputeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/disputes")
@Tag(name = "Disputes (Admin)")
public class DisputeAdminController {

    private final DisputeService service;

    public DisputeAdminController(DisputeService service) {
        this.service = service;
    }

    /** Paginated list, optionally filtered by status. */
    @GetMapping
    public ResponseEntity<Page<DisputeSummaryResponse>> list(
            @RequestParam(required = false) DisputeStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.adminList(status, pageable));
    }

    /** Post an admin message to the thread (auto-flips OPEN → UNDER_REVIEW on first reply). */
    @PostMapping("/{disputeId}/messages")
    public ResponseEntity<DisputeResponse> addMessage(
            @PathVariable Long disputeId,
            @Valid @RequestBody DisputeMessageCreateRequest req) {
        return ResponseEntity.ok(service.adminAddMessage(disputeId, req));
    }

    /** Record a formal resolution. */
    @PostMapping("/{disputeId}/resolve")
    public ResponseEntity<DisputeResponse> resolve(
            @PathVariable Long disputeId,
            @Valid @RequestBody DisputeResolutionRequest req) {
        return ResponseEntity.ok(service.adminResolve(disputeId, req));
    }
}
