package org.skillbridge.dto.response;

import org.skillbridge.enums.AiFeature;
import org.skillbridge.enums.AiUsageStatus;

import java.time.Instant;

public record AiUsageEventResponse(
        Long id,
        AiFeature feature,
        Integer cost,
        AiUsageStatus status,
        Integer tokensUsed,
        String errorMessage,
        Instant createdAt
) {}