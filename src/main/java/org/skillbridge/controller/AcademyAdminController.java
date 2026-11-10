package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.CourseCreateRequest;
import org.skillbridge.dto.request.DepartmentCreateRequest;
import org.skillbridge.dto.request.LessonCreateRequest;
import org.skillbridge.dto.request.ProgramCreateRequest;
import org.skillbridge.dto.request.SemesterCreateRequest;
import org.skillbridge.dto.request.SessionCreateRequest;
import org.skillbridge.dto.response.AcademicDepartmentResponse;
import org.skillbridge.dto.response.AcademicProgramResponse;
import org.skillbridge.dto.response.AcademicSemesterResponse;
import org.skillbridge.dto.response.AcademicSessionResponse;
import org.skillbridge.dto.response.CourseResponse;
import org.skillbridge.dto.response.LessonResponse;
import org.skillbridge.service.impl.AcademicServiceImpl;
import org.skillbridge.service.impl.LessonServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.skillbridge.dto.request.AssignmentCreateRequest;
import org.skillbridge.dto.request.GradeSubmissionRequest;
import org.skillbridge.dto.response.AssignmentResponse;
import org.skillbridge.dto.response.SubmissionResponse;
import org.skillbridge.service.impl.AssignmentServiceImpl;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/academy")
@Tag(name = "Academy Admin")
public class AcademyAdminController {

    private final AcademicServiceImpl academicService;
    private final LessonServiceImpl lessonService;
    private final AssignmentServiceImpl assignmentService;

    public AcademyAdminController(AcademicServiceImpl academicService,
                                  LessonServiceImpl lessonService,
                                  AssignmentServiceImpl assignmentService) {
        this.academicService = academicService;
        this.lessonService = lessonService;
        this.assignmentService = assignmentService;
    }

    @PostMapping("/programs")
    public ResponseEntity<AcademicProgramResponse> createProgram(@Valid @RequestBody ProgramCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicService.createProgram(req));
    }

    @PostMapping("/departments")
    public ResponseEntity<AcademicDepartmentResponse> createDepartment(@Valid @RequestBody DepartmentCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicService.createDepartment(req));
    }

    @PostMapping("/sessions")
    public ResponseEntity<AcademicSessionResponse> createSession(@Valid @RequestBody SessionCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicService.createSession(req));
    }

    @PostMapping("/semesters")
    public ResponseEntity<AcademicSemesterResponse> createSemester(@Valid @RequestBody SemesterCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicService.createSemester(req));
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicService.createCourse(req));
    }

    @PostMapping("/courses/{courseId}/lessons")
    public ResponseEntity<LessonResponse> createLesson(@PathVariable Long courseId,
                                                       @Valid @RequestBody LessonCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(courseId, req));
    }

        @PostMapping("/courses/{courseId}/assignments")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @PathVariable Long courseId,
            @Valid @RequestBody AssignmentCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.createAssignment(courseId, req));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> listSubmissions(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(assignmentService.listSubmissionsForAssignment(assignmentId));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<SubmissionResponse> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionRequest req) {
        return ResponseEntity.ok(assignmentService.gradeSubmission(submissionId, req));
    }
}