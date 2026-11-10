package org.skillbridge.dto.response;

import org.skillbridge.enums.SubmissionStatus;

import java.time.Instant;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        Long studentRecordId,
        String studentId,
        Long studentUserId,
        String studentName,
        String studentEmail,
        String content,
        String fileUrl,
        SubmissionStatus status,
        Integer score,
        String feedback,
        Instant submittedAt,
        Instant gradedAt,
        String gradedByName
) {}