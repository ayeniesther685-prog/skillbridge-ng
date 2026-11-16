package org.skillbridge.dto.response;

import org.skillbridge.enums.VerificationEventType;
import org.skillbridge.enums.VerificationStatus;

import java.time.Instant;

public record ProviderVerificationEventResponse(
        Long id,
        VerificationEventType eventType,
        VerificationStatus fromStatus,
        VerificationStatus toStatus,
        String notes,
        Long actorUserId,
        Instant createdAt
) {}
