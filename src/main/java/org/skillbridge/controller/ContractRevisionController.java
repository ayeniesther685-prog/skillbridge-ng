package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.RevisionRequestCreateRequest;
import org.skillbridge.dto.request.RevisionResponseUpdateRequest;
import org.skillbridge.dto.response.RevisionResponse;
import org.skillbridge.service.ContractRevisionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Contract Revisions")
public class ContractRevisionController {

    private final ContractRevisionService service;

    public ContractRevisionController(ContractRevisionService service) {
        this.service = service;
    }

    /**
     * CLIENT requests a revision on a SUBMITTED milestone.
     */
    @PostMapping("/milestones/{milestoneId}/revisions")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<RevisionResponse> requestRevision(
            @PathVariable Long milestoneId,
            @Valid @RequestBody RevisionRequestCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.requestRevision(milestoneId, req));
    }

    /**
     * FREELANCER responds to an OPEN revision.
     */
    @PostMapping("/revisions/{revisionId}/respond")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<RevisionResponse> respond(
            @PathVariable Long revisionId,
            @Valid @RequestBody RevisionResponseUpdateRequest req) {
        return ResponseEntity.ok(service.respondToRevision(revisionId, req));
    }

    /**
     * List revisions for a contract. Client OR freelancer on that contract.
     */
    @GetMapping("/contracts/{contractId}/revisions")
    public ResponseEntity<List<RevisionResponse>> listForContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(service.listForContract(contractId));
    }

    /**
     * List revisions for a specific milestone.
     */
    @GetMapping("/milestones/{milestoneId}/revisions")
    public ResponseEntity<List<RevisionResponse>> listForMilestone(@PathVariable Long milestoneId) {
        return ResponseEntity.ok(service.listForMilestone(milestoneId));
    }

    /**
     * Fetch a single revision.
     */
    @GetMapping("/revisions/{revisionId}")
    public ResponseEntity<RevisionResponse> get(@PathVariable Long revisionId) {
        return ResponseEntity.ok(service.findById(revisionId));
    }
}
