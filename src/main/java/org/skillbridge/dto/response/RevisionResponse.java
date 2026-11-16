package org.skillbridge.dto.response;

import org.skillbridge.enums.RevisionStatus;

import java.time.Instant;

public record RevisionResponse(
        Long id,
        Long milestoneId,
        Long contractId,
        Long requestedByUserId,
        String requestedByName,
        String reason,
        RevisionStatus status,
        boolean isNewRequirements,
        String freelancerResponse,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {}
