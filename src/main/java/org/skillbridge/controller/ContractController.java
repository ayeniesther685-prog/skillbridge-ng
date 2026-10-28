package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ContractCreateRequest;
import org.skillbridge.dto.request.ContractStatusUpdateRequest;
import org.skillbridge.dto.response.ContractResponse;
import org.skillbridge.service.impl.ContractServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contracts")
public class ContractController {

    private final ContractServiceImpl service;

    public ContractController(ContractServiceImpl service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ContractResponse> create(@Valid @RequestBody ContractCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFromApplication(req));
    }

    @GetMapping
    public ResponseEntity<Page<ContractResponse>> mine(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listMine(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ContractResponse> changeStatus(@PathVariable Long id,
                                                         @Valid @RequestBody ContractStatusUpdateRequest req) {
        return ResponseEntity.ok(service.changeStatus(id, req.status()));
    }
}