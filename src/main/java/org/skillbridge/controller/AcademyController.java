package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.EnrollCourseRequest;
import org.skillbridge.dto.request.EnrollStudentRequest;
import org.skillbridge.dto.request.ProgressUpdateRequest;
import org.skillbridge.dto.response.AcademicDepartmentResponse;
import org.skillbridge.dto.response.AcademicProgramResponse;
import org.skillbridge.dto.response.AcademicSemesterResponse;
import org.skillbridge.dto.response.AcademicSessionResponse;
import org.skillbridge.dto.response.CourseLessonsResponse;
import org.skillbridge.dto.response.CourseProgressResponse;
import org.skillbridge.dto.response.CourseResponse;
import org.skillbridge.dto.response.EnrollmentResponse;
import org.skillbridge.dto.response.LessonProgressResponse;
import org.skillbridge.dto.response.LessonResponse;
import org.skillbridge.dto.response.StudentRecordResponse;
import org.skillbridge.service.impl.AcademicServiceImpl;
import org.skillbridge.service.impl.LessonServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academy")
@Tag(name = "Academy")
public class AcademyController {

    private final AcademicServiceImpl academicService;
    private final LessonServiceImpl lessonService;

    public AcademyController(AcademicServiceImpl academicService,
                             LessonServiceImpl lessonService) {
        this.academicService = academicService;
        this.lessonService = lessonService;
    }

    // ===== Public reads =====
    @GetMapping("/programs")
    public ResponseEntity<List<AcademicProgramResponse>> programs() {
        return ResponseEntity.ok(academicService.listPrograms());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<AcademicDepartmentResponse>> departments(@RequestParam Long programId) {
        return ResponseEntity.ok(academicService.listDepartments(programId));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> courses(@RequestParam Long departmentId) {
        return ResponseEntity.ok(academicService.listCourses(departmentId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AcademicSessionResponse>> sessions() {
        return ResponseEntity.ok(academicService.listSessions());
    }

    @GetMapping("/sessions/{sessionId}/semesters")
    public ResponseEntity<List<AcademicSemesterResponse>> semesters(@PathVariable Long sessionId) {
        return ResponseEntity.ok(academicService.listSemesters(sessionId));
    }

    // ===== Student enrollment =====
    @PostMapping("/enroll")
    public ResponseEntity<StudentRecordResponse> enroll(@Valid @RequestBody EnrollStudentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicService.enrollMe(req));
    }

    @GetMapping("/me/record")
    public ResponseEntity<StudentRecordResponse> myRecord() {
        return ResponseEntity.ok(academicService.getMyRecord());
    }

    @GetMapping("/me/enrollments")
    public ResponseEntity<List<EnrollmentResponse>> myEnrollments() {
        return ResponseEntity.ok(academicService.listMyEnrollments());
    }

    @PostMapping("/me/enrollments")
    public ResponseEntity<EnrollmentResponse> enrollCourse(@Valid @RequestBody EnrollCourseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicService.enrollInCourse(req));
    }

    // ===== Lessons (public preview) =====
    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<CourseLessonsResponse> listLessons(@PathVariable Long courseId) {
        return ResponseEntity.ok(lessonService.listLessons(courseId));
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> getLesson(@PathVariable Long courseId,
                                                    @PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonService.getLesson(courseId, lessonId));
    }

    // ===== Progress (student only) =====
    @PutMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<LessonProgressResponse> updateProgress(
            @PathVariable Long lessonId,
            @Valid @RequestBody ProgressUpdateRequest req) {
        return ResponseEntity.ok(lessonService.updateMyProgress(lessonId, req.status()));
    }

    @GetMapping("/me/courses/{courseId}/progress")
    public ResponseEntity<CourseProgressResponse> myProgress(@PathVariable Long courseId) {
        return ResponseEntity.ok(lessonService.getMyCourseProgress(courseId));
    }
}