package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.ReviewCreateRequest;
import org.skillbridge.dto.response.ReviewResponse;
import org.skillbridge.service.impl.ReviewServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewServiceImpl service;

    public ReviewController(ReviewServiceImpl service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<ReviewResponse>> mine(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listMine(pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewResponse>> forUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listForUser(userId, pageable));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<Page<ReviewResponse>> forJob(
            @PathVariable Long jobId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listForJob(jobId, pageable));
    }

    @GetMapping("/user/{userId}/rating")
    public ResponseEntity<Map<String, Object>> ratingForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "average", service.averageForUser(userId)
        ));
    }
}