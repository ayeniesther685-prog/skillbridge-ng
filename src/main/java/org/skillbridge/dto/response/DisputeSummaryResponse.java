package org.skillbridge.dto.response;

import org.skillbridge.enums.DisputeStatus;

import java.time.Instant;

public record DisputeSummaryResponse(
        Long id,
        Long contractId,
        Long milestoneId,
        Long openedByUserId,
        String openedByName,
        String reason,
        DisputeStatus status,
        Instant createdAt,
        Instant resolvedAt
) {}
