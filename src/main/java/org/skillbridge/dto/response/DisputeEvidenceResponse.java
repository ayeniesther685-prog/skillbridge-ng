package org.skillbridge.dto.response;

import org.skillbridge.enums.DisputeEvidenceType;

import java.time.Instant;

public record DisputeEvidenceResponse(
        Long id,
        Long uploadedByUserId,
        String uploadedByName,
        DisputeEvidenceType evidenceType,
        String url,
        String description,
        Instant createdAt
) {}
