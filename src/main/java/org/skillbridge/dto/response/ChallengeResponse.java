package org.skillbridge.dto.response;

import java.time.Instant;

public record ChallengeResponse(
        Long id,
        String title,
        String description,
        Integer pointReward,
        String difficulty,
        Integer maxWinners,
        Integer winnersCount,
        Instant activeFrom,
        Instant activeUntil,
        boolean active,
        long submissionCount
) {}