package org.skillbridge.dto.response;

import org.skillbridge.enums.ChallengeSubmissionStatus;

import java.time.Instant;

public record ChallengeSubmissionResponse(
        Long id,
        Long challengeId,
        String challengeTitle,
        Long userId,
        String userName,
        String content,
        ChallengeSubmissionStatus status,
        Integer pointsAwarded,
        String adminFeedback,
        Instant submittedAt,
        Instant reviewedAt
) {}