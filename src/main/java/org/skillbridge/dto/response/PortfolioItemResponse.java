package org.skillbridge.dto.response;

import java.time.Instant;

public record PortfolioItemResponse(
        Long id,
        Long userId,
        String title,
        String description,
        String imageUrl,
        String projectUrl,
        String technologies,
        Instant createdAt
) {}