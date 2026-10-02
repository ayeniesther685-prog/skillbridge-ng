package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ProposalCreateRequest;
import org.skillbridge.dto.response.ProposalResponse;
import org.skillbridge.service.impl.ProposalServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Proposals")
public class ProposalController {

    private final ProposalServiceImpl service;

    public ProposalController(ProposalServiceImpl service) { this.service = service; }

    @GetMapping("/applications/{applicationId}/proposal")
    public ResponseEntity<ProposalResponse> get(@PathVariable Long applicationId) {
        return ResponseEntity.ok(service.findByApplication(applicationId));
    }

    @PutMapping("/applications/{applicationId}/proposal")
    public ResponseEntity<ProposalResponse> update(@PathVariable Long applicationId,
                                                   @Valid @RequestBody ProposalCreateRequest req) {
        return ResponseEntity.ok(service.update(applicationId, req));
    }
}