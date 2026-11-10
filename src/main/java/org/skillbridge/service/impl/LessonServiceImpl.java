package org.skillbridge.service.impl;

import org.skillbridge.dto.request.LessonCreateRequest;
import org.skillbridge.dto.response.CourseLessonsResponse;
import org.skillbridge.dto.response.CourseProgressResponse;
import org.skillbridge.dto.response.LessonProgressResponse;
import org.skillbridge.dto.response.LessonResponse;
import org.skillbridge.dto.response.LessonSummaryResponse;
import org.skillbridge.entity.Course;
import org.skillbridge.entity.CourseEnrollment;
import org.skillbridge.entity.CourseLesson;
import org.skillbridge.entity.LessonProgress;
import org.skillbridge.entity.StudentRecord;
import org.skillbridge.entity.User;
import org.skillbridge.enums.EnrollmentStatus;
import org.skillbridge.enums.LessonProgressStatus;
import org.skillbridge.exception.DuplicateResourceException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.LessonMapper;
import org.skillbridge.repository.CourseEnrollmentRepository;
import org.skillbridge.repository.CourseLessonRepository;
import org.skillbridge.repository.CourseRepository;
import org.skillbridge.repository.LessonProgressRepository;
import org.skillbridge.repository.StudentRecordRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class LessonServiceImpl {

    private final CourseLessonRepository lessonRepository;
    private final LessonProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final StudentRecordRepository studentRecordRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonMapper mapper;
    private final CurrentUser currentUser;

    public LessonServiceImpl(CourseLessonRepository lessonRepository,
                             LessonProgressRepository progressRepository,
                             CourseRepository courseRepository,
                             StudentRecordRepository studentRecordRepository,
                             CourseEnrollmentRepository enrollmentRepository,
                             LessonMapper mapper,
                             CurrentUser currentUser) {
        this.lessonRepository = lessonRepository;
        this.progressRepository = progressRepository;
        this.courseRepository = courseRepository;
        this.studentRecordRepository = studentRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

   public CourseLessonsResponse listLessons(Long courseId) {
    Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

    List<LessonSummaryResponse> items = lessonRepository
            .findByCourseIdOrderByOrderNumberAsc(courseId)
            .stream().map(mapper::toSummary).toList();

    return new CourseLessonsResponse(course.getId(), course.getTitle(), items.size(), items);
}

    public LessonResponse getLesson(Long courseId, Long lessonId) {
        CourseLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Lesson not in that course");
        }
        return mapper.toResponse(lesson);
    }

    @Transactional
    public LessonResponse createLesson(Long courseId, LessonCreateRequest req) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (lessonRepository.existsByCourseIdAndOrderNumber(courseId, req.orderNumber())) {
            throw new DuplicateResourceException("A lesson with order number "
                    + req.orderNumber() + " already exists in this course");
        }

        CourseLesson lesson = new CourseLesson();
        lesson.setCourse(course);
        lesson.setTitle(req.title());
        lesson.setContent(req.content());
        lesson.setOrderNumber(req.orderNumber());
        lesson.setDurationMinutes(req.durationMinutes() == null ? 30 : req.durationMinutes());
        lesson.setLearningObjectives(req.learningObjectives());
        lesson.setVideoUrl(req.videoUrl());

        return mapper.toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonProgressResponse updateMyProgress(Long lessonId, LessonProgressStatus newStatus) {
        User me = currentUser.require();
        StudentRecord record = studentRecordRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enroll as a student first"));

        CourseLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        requireEnrollment(record.getId(), lesson.getCourse().getId());

        LessonProgress progress = progressRepository
                .findByStudentRecordIdAndLessonId(record.getId(), lessonId)
                .orElseGet(() -> {
                    LessonProgress p = new LessonProgress();
                    p.setStudentRecord(record);
                    p.setLesson(lesson);
                    return p;
                });

        // Enforce sane transitions: COMPLETED is terminal, can't go back.
        if (progress.getStatus() == LessonProgressStatus.COMPLETED
                && newStatus != LessonProgressStatus.COMPLETED) {
            throw new IllegalStateException("Cannot un-complete a completed lesson");
        }

        progress.setStatus(newStatus);
        if (newStatus == LessonProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
        }

        LessonProgress saved = progressRepository.save(progress);
        return mapper.toProgressResponse(lesson, saved);
    }

    public CourseProgressResponse getMyCourseProgress(Long courseId) {
        User me = currentUser.require();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        StudentRecord record = studentRecordRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enroll as a student first"));

        requireEnrollment(record.getId(), courseId);

        List<CourseLesson> lessons = lessonRepository.findByCourseIdOrderByOrderNumberAsc(courseId);
        List<LessonProgressResponse> items = new ArrayList<>();
        long completed = 0;
        for (CourseLesson lesson : lessons) {
            Optional<LessonProgress> progress = progressRepository
                    .findByStudentRecordIdAndLessonId(record.getId(), lesson.getId());
            LessonProgressResponse resp = mapper.toProgressResponse(lesson, progress.orElse(null));
            items.add(resp);
            if (resp.status() == LessonProgressStatus.COMPLETED) {
                completed++;
            }
        }

        int percent = lessons.isEmpty() ? 0
                : (int) Math.round((completed * 100.0) / lessons.size());

        return new CourseProgressResponse(
                course.getId(), course.getTitle(),
                lessons.size(), completed, percent, items);
    }

    private void requireEnrollment(Long studentRecordId, Long courseId) {
        List<CourseEnrollment> all = enrollmentRepository
                .findByStudentRecordIdOrderByEnrolledAtDesc(studentRecordId);
        boolean enrolled = all.stream().anyMatch(e ->
                e.getCourse().getId().equals(courseId)
                        && e.getStatus() != EnrollmentStatus.WITHDRAWN);
        if (!enrolled) {
            throw new AccessDeniedException("You are not enrolled in this course");
        }
    }
}