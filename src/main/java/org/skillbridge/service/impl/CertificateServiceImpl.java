package org.skillbridge.service.impl;

import org.skillbridge.dto.response.CertificateResponse;
import org.skillbridge.dto.response.CertificateVerificationResponse;
import org.skillbridge.entity.AssignmentSubmission;
import org.skillbridge.entity.Certificate;
import org.skillbridge.entity.Course;
import org.skillbridge.entity.CourseEnrollment;
import org.skillbridge.entity.StudentRecord;
import org.skillbridge.entity.User;
import org.skillbridge.enums.CertificateStatus;
import org.skillbridge.enums.EnrollmentStatus;
import org.skillbridge.enums.SubmissionStatus;
import org.skillbridge.exception.DuplicateResourceException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.CertificateMapper;
import org.skillbridge.repository.AssignmentSubmissionRepository;
import org.skillbridge.repository.CertificateRepository;
import org.skillbridge.repository.CourseEnrollmentRepository;
import org.skillbridge.repository.CourseRepository;
import org.skillbridge.repository.StudentRecordRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CertificateServiceImpl {

    private static final int PASSING_SCORE = 60;

    private final CertificateRepository certificateRepository;
    private final StudentRecordRepository studentRecordRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CertificateMapper mapper;
    private final CurrentUser currentUser;

    public CertificateServiceImpl(CertificateRepository certificateRepository,
                                  StudentRecordRepository studentRecordRepository,
                                  CourseRepository courseRepository,
                                  CourseEnrollmentRepository enrollmentRepository,
                                  AssignmentSubmissionRepository submissionRepository,
                                  CertificateMapper mapper,
                                  CurrentUser currentUser) {
        this.certificateRepository = certificateRepository;
        this.studentRecordRepository = studentRecordRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.submissionRepository = submissionRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Transactional
    public CertificateResponse issueCertificate(Long courseId) {
        User me = currentUser.require();
        StudentRecord record = studentRecordRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not enrolled as a student"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        requireEnrollment(record.getId(), courseId);

        if (certificateRepository.existsByStudentRecordIdAndCourseId(record.getId(), courseId)) {
            throw new DuplicateResourceException("You already have a certificate for this course");
        }

        List<AssignmentSubmission> graded = submissionRepository
                .findByStudentRecordIdAndStatus(record.getId(), SubmissionStatus.GRADED);
        List<AssignmentSubmission> forThisCourse = graded.stream()
                .filter(s -> s.getAssignment().getCourse().getId().equals(courseId))
                .collect(Collectors.toList());

        if (forThisCourse.isEmpty()) {
            throw new IllegalStateException(
                    "You need at least one graded assignment in this course before requesting a certificate");
        }

        double avg = forThisCourse.stream()
                .mapToInt(AssignmentSubmission::getScore)
                .average()
                .orElse(0.0);

        int finalScore = (int) Math.round(avg);
        if (finalScore < PASSING_SCORE) {
            throw new IllegalStateException(
                    "Your average score is " + finalScore + "%. Minimum required is " + PASSING_SCORE + "%");
        }

        long seq = certificateRepository.count() + 1;
        String certNumber = String.format("SB-CERT-%d-%06d", Year.now().getValue(), seq);
        String hashInput = certNumber + "|" + record.getStudentId() + "|" + courseId + "|" + System.currentTimeMillis();
        String hash = sha256Hex(hashInput);

        Certificate cert = new Certificate();
        cert.setStudentRecord(record);
        cert.setCourse(course);
        cert.setCertificateNumber(certNumber);
        cert.setFinalScore(finalScore);
        cert.setStatus(CertificateStatus.ISSUED);
        cert.setVerificationHash(hash);

        return mapper.toResponse(certificateRepository.save(cert));
    }

    public List<CertificateResponse> listMyCertificates() {
        User me = currentUser.require();
        StudentRecord record = studentRecordRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not enrolled as a student"));
        return certificateRepository.findByStudentRecordIdOrderByIssuedAtDesc(record.getId())
                .stream().map(mapper::toResponse).toList();
    }

    public CertificateVerificationResponse verify(String certificateNumber) {
        Certificate cert = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
        return mapper.toVerificationResponse(cert);
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

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}