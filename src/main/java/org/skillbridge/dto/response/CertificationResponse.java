package org.skillbridge.dto.response;

import java.time.LocalDate;

public record CertificationResponse(
        Long id,
        Long userId,
        String name,
        String issuingOrganization,
        LocalDate issueDate,
        LocalDate expiryDate,
        String credentialUrl
) {}