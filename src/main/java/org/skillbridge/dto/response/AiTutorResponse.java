package org.skillbridge.dto.response;

public record AiTutorResponse(
        String response,
        Integer tokensUsed,
        Integer remainingToday
) {}