package org.skillbridge.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ProfileResponse(
        Long id,
        Long userId,
        String headline,
        String website,
        String linkedinUrl,
        String githubUrl,
        String twitterUrl,
        String availability,
        BigDecimal hourlyRate,
        Instant createdAt,
        Instant updatedAt
) {}