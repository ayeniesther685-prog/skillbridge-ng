package org.skillbridge.dto.response;

import java.time.Instant;

public record CertificateResponse(
        Long id,
        String certificateNumber,
        Long courseId,
        String courseTitle,
        String studentId,
        String studentName,
        String studentEmail,
        Integer finalScore,
        String status,
        Instant issuedAt,
        String verifyUrl
) {}