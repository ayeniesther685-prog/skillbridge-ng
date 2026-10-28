package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ConversationCreateRequest;
import org.skillbridge.dto.response.ConversationResponse;
import org.skillbridge.service.impl.ConversationServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Conversations")
public class ConversationController {

    private final ConversationServiceImpl service;

    public ConversationController(ConversationServiceImpl service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<ConversationResponse>> mine(
            @PageableDefault(size = 20, sort = "lastMessageAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listMine(pageable));
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> create(@Valid @RequestBody ConversationCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.findOrCreate(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}