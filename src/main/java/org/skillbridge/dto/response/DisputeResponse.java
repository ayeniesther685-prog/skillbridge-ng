package org.skillbridge.dto.response;

import org.skillbridge.enums.DisputeStatus;

import java.time.Instant;
import java.util.List;

public record DisputeResponse(
        Long id,
        Long contractId,
        Long milestoneId,
        String contractTitle,
        Long openedByUserId,
        String openedByName,
        String reason,
        DisputeStatus status,
        String resolution,
        Long resolvedByUserId,
        String resolvedByName,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt,
        List<DisputeMessageResponse> messages,
        List<DisputeEvidenceResponse> evidence
) {}
