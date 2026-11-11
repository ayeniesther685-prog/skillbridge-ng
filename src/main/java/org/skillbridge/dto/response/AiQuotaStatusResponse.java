package org.skillbridge.dto.response;

import java.time.Instant;

public record AiQuotaStatusResponse(
        Long userId,
        String tierName,
        Integer dailyLimit,
        Integer monthlyLimit,
        Integer usedToday,
        Integer usedThisMonth,
        Integer bonusCredits,
        Integer remainingToday,
        Integer remainingThisMonth,
        Instant dailyResetAt,
        Instant monthlyResetAt
) {}