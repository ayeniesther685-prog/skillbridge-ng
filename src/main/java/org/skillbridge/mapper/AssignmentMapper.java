package org.skillbridge.mapper;

import org.skillbridge.dto.response.AssignmentResponse;
import org.skillbridge.dto.response.AssignmentSummaryResponse;
import org.skillbridge.dto.response.SubmissionResponse;
import org.skillbridge.entity.Assignment;
import org.skillbridge.entity.AssignmentSubmission;
import org.skillbridge.entity.StudentRecord;
import org.skillbridge.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {

    public AssignmentSummaryResponse toSummary(Assignment a) {
        return new AssignmentSummaryResponse(
                a.getId(),
                a.getCourse().getId(),
                a.getTitle(),
                a.getDescription(),
                a.getMaxScore(),
                a.getWeight(),
                a.getDueDate(),
                a.isPublished()
        );
    }

    public AssignmentResponse toResponse(Assignment a) {
        return new AssignmentResponse(
                a.getId(),
                a.getCourse().getId(),
                a.getCourse().getTitle(),
                a.getTitle(),
                a.getDescription(),
                a.getInstructions(),
                a.getMaxScore(),
                a.getWeight(),
                a.getDueDate(),
                a.isPublished(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    public SubmissionResponse toResponse(AssignmentSubmission s) {
        StudentRecord sr = s.getStudentRecord();
        User student = sr.getUser();
        String studentName = (student.getFirstName() + " " + student.getLastName()).trim();
        User grader = s.getGradedBy();
        String graderName = grader == null ? null
                : (grader.getFirstName() + " " + grader.getLastName()).trim();

        return new SubmissionResponse(
                s.getId(),
                s.getAssignment().getId(),
                s.getAssignment().getTitle(),
                sr.getId(),
                sr.getStudentId(),
                student.getId(),
                studentName,
                student.getEmail(),
                s.getContent(),
                s.getFileUrl(),
                s.getStatus(),
                s.getScore(),
                s.getFeedback(),
                s.getSubmittedAt(),
                s.getGradedAt(),
                graderName
        );
    }
}