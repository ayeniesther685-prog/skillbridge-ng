package org.skillbridge.dto.response;

import org.skillbridge.enums.EnrollmentStatus;

import java.time.Instant;

public record EnrollmentResponse(
        Long id,
        Long courseId,
        String courseCode,
        String courseTitle,
        Long semesterId,
        String semesterName,
        EnrollmentStatus status,
        String finalGrade,
        Instant enrolledAt
) {}