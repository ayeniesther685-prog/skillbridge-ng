package org.skillbridge.dto.response;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long reviewerId,
        String reviewerName,
        Long revieweeId,
        String revieweeName,
        Long jobId,
        String jobTitle,
        Short rating,
        String comment,
        Instant createdAt
) {}