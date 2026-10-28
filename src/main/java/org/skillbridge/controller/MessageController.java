package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.MessageCreateRequest;
import org.skillbridge.dto.response.MessageResponse;
import org.skillbridge.service.impl.MessageServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Messages")
public class MessageController {

    private final MessageServiceImpl service;

    public MessageController(MessageServiceImpl service) { this.service = service; }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Page<MessageResponse>> list(
            @PathVariable Long conversationId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.listForConversation(conversationId, pageable));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageResponse> send(@PathVariable Long conversationId,
                                                @Valid @RequestBody MessageCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.send(conversationId, req));
    }

    @PutMapping("/messages/{id}/read")
    public ResponseEntity<MessageResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(service.markRead(id));
    }
}
