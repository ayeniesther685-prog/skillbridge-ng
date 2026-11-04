package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.*;
import org.skillbridge.dto.response.*;
import org.skillbridge.service.impl.AcademicServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/academy")
@Tag(name = "Academy Admin")
public class AcademyAdminController {

    private final AcademicServiceImpl service;

    public AcademyAdminController(AcademicServiceImpl service) { this.service = service; }

    @PostMapping("/programs")
    public ResponseEntity<AcademicProgramResponse> createProgram(@Valid @RequestBody ProgramCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createProgram(req));
    }

    @PostMapping("/departments")
    public ResponseEntity<AcademicDepartmentResponse> createDepartment(@Valid @RequestBody DepartmentCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDepartment(req));
    }

    @PostMapping("/sessions")
    public ResponseEntity<AcademicSessionResponse> createSession(@Valid @RequestBody SessionCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSession(req));
    }

    @PostMapping("/semesters")
    public ResponseEntity<AcademicSemesterResponse> createSemester(@Valid @RequestBody SemesterCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSemester(req));
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCourse(req));
    }
}