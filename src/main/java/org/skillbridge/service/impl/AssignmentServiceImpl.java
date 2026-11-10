package org.skillbridge.service.impl;

import org.skillbridge.dto.request.AssignmentCreateRequest;
import org.skillbridge.dto.request.GradeSubmissionRequest;
import org.skillbridge.dto.request.SubmissionCreateRequest;
import org.skillbridge.dto.response.AssignmentResponse;
import org.skillbridge.dto.response.AssignmentSummaryResponse;
import org.skillbridge.dto.response.SubmissionResponse;
import org.skillbridge.entity.Assignment;
import org.skillbridge.entity.AssignmentSubmission;
import org.skillbridge.entity.Course;
import org.skillbridge.entity.CourseEnrollment;
import org.skillbridge.entity.StudentRecord;
import org.skillbridge.entity.User;
import org.skillbridge.enums.EnrollmentStatus;
import org.skillbridge.enums.SubmissionStatus;
import org.skillbridge.exception.DuplicateResourceException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.AssignmentMapper;
import org.skillbridge.repository.AssignmentRepository;
import org.skillbridge.repository.AssignmentSubmissionRepository;
import org.skillbridge.repository.CourseEnrollmentRepository;
import org.skillbridge.repository.CourseRepository;
import org.skillbridge.repository.StudentRecordRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AssignmentServiceImpl {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final StudentRecordRepository studentRecordRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final AssignmentMapper mapper;
    private final CurrentUser currentUser;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
                                 AssignmentSubmissionRepository submissionRepository,
                                 CourseRepository courseRepository,
                                 StudentRecordRepository studentRecordRepository,
                                 CourseEnrollmentRepository enrollmentRepository,
                                 AssignmentMapper mapper,
                                 CurrentUser currentUser) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.studentRecordRepository = studentRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public List<AssignmentSummaryResponse> listPublishedForCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }
        return assignmentRepository.findByCourseIdAndPublishedTrueOrderByCreatedAtAsc(courseId)
                .stream().map(mapper::toSummary).toList();
    }

    public AssignmentResponse getAssignment(Long id) {
        Assignment a = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        return mapper.toResponse(a);
    }

    @Transactional
    public AssignmentResponse createAssignment(Long courseId, AssignmentCreateRequest req) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (assignmentRepository.existsByCourseIdAndTitle(courseId, req.title())) {
            throw new DuplicateResourceException("An assignment with this title already exists in this course");
        }

        Assignment a = new Assignment();
        a.setCourse(course);
        a.setTitle(req.title());
        a.setDescription(req.description());
        a.setInstructions(req.instructions());
        a.setMaxScore(req.maxScore() == null ? 100 : req.maxScore());
        a.setWeight(req.weight() == null ? BigDecimal.ZERO : req.weight());
        a.setDueDate(req.dueDate());
        a.setPublished(Boolean.TRUE.equals(req.published()));

        return mapper.toResponse(assignmentRepository.save(a));
    }

    @Transactional
    public SubmissionResponse submitAssignment(Long assignmentId, SubmissionCreateRequest req) {
        User me = currentUser.require();
        StudentRecord record = studentRecordRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enroll as a student first"));

        Assignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!a.isPublished()) {
            throw new AccessDeniedException("This assignment is not yet available");
        }
        requireEnrollment(record.getId(), a.getCourse().getId());

        if (submissionRepository.findByAssignmentIdAndStudentRecordId(assignmentId, record.getId()).isPresent()) {
            throw new DuplicateResourceException("You have already submitted this assignment");
        }

        if ((req.content() == null || req.content().isBlank())
                && (req.fileUrl() == null || req.fileUrl().isBlank())) {
            throw new IllegalArgumentException("Submission must include either content or a file URL");
        }

        AssignmentSubmission s = new AssignmentSubmission();
        s.setAssignment(a);
        s.setStudentRecord(record);
        s.setContent(req.content());
        s.setFileUrl(req.fileUrl());
        s.setStatus(SubmissionStatus.SUBMITTED);

        return mapper.toResponse(submissionRepository.save(s));
    }

    public SubmissionResponse getMySubmission(Long assignmentId) {
        User me = currentUser.require();
        StudentRecord record = studentRecordRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not enrolled as a student"));
        AssignmentSubmission s = submissionRepository
                .findByAssignmentIdAndStudentRecordId(assignmentId, record.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You have not submitted this assignment yet"));
        return mapper.toResponse(s);
    }

    public List<SubmissionResponse> listSubmissionsForAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("Assignment not found");
        }
        return submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest req) {
        User grader = currentUser.require();
        AssignmentSubmission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (req.score() < 0 || req.score() > s.getAssignment().getMaxScore()) {
            throw new IllegalArgumentException(
                    "Score must be between 0 and " + s.getAssignment().getMaxScore());
        }

        s.setScore(req.score());
        s.setFeedback(req.feedback());
        s.setStatus(SubmissionStatus.GRADED);
        s.setGradedAt(Instant.now());
        s.setGradedBy(grader);

        return mapper.toResponse(submissionRepository.save(s));
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