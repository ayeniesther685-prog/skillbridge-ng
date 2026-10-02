
package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.PortfolioItemRequest;
import org.skillbridge.dto.response.PortfolioItemResponse;
import org.skillbridge.service.impl.PortfolioItemServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Portfolio")
public class PortfolioController {

    private final PortfolioItemServiceImpl service;

    public PortfolioController(PortfolioItemServiceImpl service) { this.service = service; }

    @GetMapping("/users/{userId}/portfolio")
    public ResponseEntity<List<PortfolioItemResponse>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listForUser(userId));
    }

    @PostMapping("/users/me/portfolio")
    public ResponseEntity<PortfolioItemResponse> create(@Valid @RequestBody PortfolioItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/portfolio/{id}")
    public ResponseEntity<PortfolioItemResponse> update(@PathVariable Long id, @Valid @RequestBody PortfolioItemRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/portfolio/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}