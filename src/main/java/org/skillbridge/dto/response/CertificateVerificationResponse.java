package org.skillbridge.dto.response;

import java.time.Instant;

public record CertificateVerificationResponse(
        boolean valid,
        String certificateNumber,
        String studentName,
        String studentId,
        String courseTitle,
        Integer finalScore,
        Instant issuedAt,
        String reason
) {}