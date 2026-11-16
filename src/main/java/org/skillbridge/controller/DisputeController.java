package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.DisputeEvidenceCreateRequest;
import org.skillbridge.dto.request.DisputeMessageCreateRequest;
import org.skillbridge.dto.request.OpenDisputeRequest;
import org.skillbridge.dto.response.DisputeResponse;
import org.skillbridge.dto.response.DisputeSummaryResponse;
import org.skillbridge.service.DisputeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Disputes")
public class DisputeController {

    private final DisputeService service;

    public DisputeController(DisputeService service) {
        this.service = service;
    }

    /** Open a dispute on a contract (or a specific milestone). */
    @PostMapping("/contracts/{contractId}/disputes")
    public ResponseEntity<DisputeResponse> open(
            @PathVariable Long contractId,
            @Valid @RequestBody OpenDisputeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.openDispute(contractId, req));
    }

    /** List all disputes for a contract (client or freelancer on it). */
    @GetMapping("/contracts/{contractId}/disputes")
    public ResponseEntity<List<DisputeSummaryResponse>> listForContract(
            @PathVariable Long contractId) {
        return ResponseEntity.ok(service.listForContract(contractId));
    }

    /** Full dispute detail with thread + evidence. */
    @GetMapping("/disputes/{disputeId}")
    public ResponseEntity<DisputeResponse> get(@PathVariable Long disputeId) {
        return ResponseEntity.ok(service.findById(disputeId));
    }

    /** Post a message to the dispute thread. */
    @PostMapping("/disputes/{disputeId}/messages")
    public ResponseEntity<DisputeResponse> addMessage(
            @PathVariable Long disputeId,
            @Valid @RequestBody DisputeMessageCreateRequest req) {
        return ResponseEntity.ok(service.addMessage(disputeId, req));
    }

    /** Attach evidence (URL reference) to the dispute. */
    @PostMapping("/disputes/{disputeId}/evidence")
    public ResponseEntity<DisputeResponse> addEvidence(
            @PathVariable Long disputeId,
            @Valid @RequestBody DisputeEvidenceCreateRequest req) {
        return ResponseEntity.ok(service.addEvidence(disputeId, req));
    }

    /** Withdraw your own dispute (opener only, while still active). */
    @PostMapping("/disputes/{disputeId}/withdraw")
    public ResponseEntity<DisputeResponse> withdraw(@PathVariable Long disputeId) {
        return ResponseEntity.ok(service.withdraw(disputeId));
    }
}
