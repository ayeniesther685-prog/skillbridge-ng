package org.skillbridge.dto.response;

import org.skillbridge.enums.ProviderType;
import org.skillbridge.enums.VerificationStatus;

import java.time.Instant;

public record ProviderVerificationResponse(
        Long id,
        Long userId,
        ProviderType providerType,
        String legalName,
        String cacRegistrationNumber,
        String officialEmail,
        String officialPhone,
        String physicalAddress,
        String website,
        String linkedinUrl,
        String authorizedRepName,
        String authorizedRepRole,
        VerificationStatus status,
        Instant submittedAt,
        Instant verifiedAt,
        Instant expiresAt,
        String adminNotes,
        Instant createdAt,
        Instant updatedAt
) {}