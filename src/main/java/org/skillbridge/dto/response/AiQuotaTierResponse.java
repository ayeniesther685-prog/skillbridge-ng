package org.skillbridge.dto.response;

public record AiQuotaTierResponse(
        Long id,
        String tierName,
        Integer dailyLimit,
        Integer monthlyLimit,
        boolean active
) {}