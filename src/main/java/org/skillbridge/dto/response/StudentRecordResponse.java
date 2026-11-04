package org.skillbridge.dto.response;

import org.skillbridge.enums.StudentStatus;

import java.time.LocalDate;

public record StudentRecordResponse(
        Long id,
        Long userId,
        String userEmail,
        String studentId,
        Long programId,
        String programName,
        Long departmentId,
        String departmentName,
        LocalDate admissionDate,
        StudentStatus status
) {}