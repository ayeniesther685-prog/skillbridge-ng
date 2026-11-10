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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/academy")
@Tag(name = "Academy Admin")
public class AcademyAdminController {

    private final AcademicServiceImpl academicService;
    private final LessonServiceImpl lessonService;

    public AcademyAdminController(AcademicServiceImpl academicService,
                                  LessonServiceImpl lessonService) {
        this.academicService = academicService;
        this.lessonService = lessonService;
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
}