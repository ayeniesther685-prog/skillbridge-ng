package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.MilestoneCreateRequest;
import org.skillbridge.dto.request.MilestoneUpdateRequest;
import org.skillbridge.dto.response.MilestoneResponse;
import org.skillbridge.enums.MilestoneStatus;
import org.skillbridge.service.impl.MilestoneServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Milestones")
public class MilestoneController {

    private final MilestoneServiceImpl service;

    public MilestoneController(MilestoneServiceImpl service) {
        this.service = service;
    }

    /** List milestones for a contract (client or freelancer). */
    @GetMapping("/contracts/{contractId}/milestones")
    public ResponseEntity<List<MilestoneResponse>> listForContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(service.listForContract(contractId));
    }

    /** Add a milestone (client only). */
    @PostMapping("/contracts/{contractId}/milestones")
    public ResponseEntity<MilestoneResponse> create(
            @PathVariable Long contractId,
            @Valid @RequestBody MilestoneCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(contractId, req));
    }

    /** Edit a milestone (client only, PENDING only). */
    @PutMapping("/milestones/{id}")
    public ResponseEntity<MilestoneResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MilestoneUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    /** Change milestone status (freelancer progresses, client approves). */
    @PutMapping("/milestones/{id}/status")
    public ResponseEntity<MilestoneResponse> changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String raw = body.get("status");
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        MilestoneStatus newStatus;
        try {
            newStatus = MilestoneStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid milestone status: " + raw);
        }
        return ResponseEntity.ok(service.changeStatus(id, newStatus));
    }

    /** Delete a milestone (client only, PENDING only). */
    @DeleteMapping("/milestones/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
