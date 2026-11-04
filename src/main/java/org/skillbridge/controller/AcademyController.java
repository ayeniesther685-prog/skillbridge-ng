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

import java.util.List;

@RestController
@RequestMapping("/api/academy")
@Tag(name = "Academy")
public class AcademyController {

    private final AcademicServiceImpl service;

    public AcademyController(AcademicServiceImpl service) { this.service = service; }

    // ===== Public reads =====
    @GetMapping("/programs")
    public ResponseEntity<List<AcademicProgramResponse>> programs() {
        return ResponseEntity.ok(service.listPrograms());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<AcademicDepartmentResponse>> departments(@RequestParam Long programId) {
        return ResponseEntity.ok(service.listDepartments(programId));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> courses(@RequestParam Long departmentId) {
        return ResponseEntity.ok(service.listCourses(departmentId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AcademicSessionResponse>> sessions() {
        return ResponseEntity.ok(service.listSessions());
    }

    @GetMapping("/sessions/{sessionId}/semesters")
    public ResponseEntity<List<AcademicSemesterResponse>> semesters(@PathVariable Long sessionId) {
        return ResponseEntity.ok(service.listSemesters(sessionId));
    }

    // ===== Student =====
    @PostMapping("/enroll")
    public ResponseEntity<StudentRecordResponse> enroll(@Valid @RequestBody EnrollStudentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enrollMe(req));
    }

    @GetMapping("/me/record")
    public ResponseEntity<StudentRecordResponse> myRecord() {
        return ResponseEntity.ok(service.getMyRecord());
    }

    @GetMapping("/me/enrollments")
    public ResponseEntity<List<EnrollmentResponse>> myEnrollments() {
        return ResponseEntity.ok(service.listMyEnrollments());
    }

    @PostMapping("/me/enrollments")
    public ResponseEntity<EnrollmentResponse> enrollCourse(@Valid @RequestBody EnrollCourseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enrollInCourse(req));
    }
}