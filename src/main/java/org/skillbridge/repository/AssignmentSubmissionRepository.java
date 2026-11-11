package org.skillbridge.repository;

import org.skillbridge.entity.AssignmentSubmission;
import org.skillbridge.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentRecordId(Long assignmentId, Long studentRecordId);
    List<AssignmentSubmission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);
    long countByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status);
    List<AssignmentSubmission> findByStudentRecordIdAndStatus(Long studentRecordId, org.skillbridge.enums.SubmissionStatus status);
}
