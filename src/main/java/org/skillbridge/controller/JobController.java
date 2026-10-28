package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.JobCreateRequest;
import org.skillbridge.dto.request.JobStatusUpdateRequest;
import org.skillbridge.dto.request.JobUpdateRequest;
import org.skillbridge.dto.response.JobResponse;
import org.skillbridge.enums.ExperienceLevel;
import org.skillbridge.enums.JobStatus;
import org.skillbridge.enums.JobType;
import org.skillbridge.service.impl.JobServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs")
public class JobController {

    private final JobServiceImpl service;

    public JobController(JobServiceImpl service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) List<Long> skill,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.search(
                search, location, jobType, experienceLevel, status,
                minBudget, maxBudget, skill, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

      @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> update(@PathVariable Long id,
                                              @Valid @RequestBody JobUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<JobResponse> changeStatus(@PathVariable Long id,
                                                    @Valid @RequestBody JobStatusUpdateRequest req) {
        return ResponseEntity.ok(service.changeStatus(id, req.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}