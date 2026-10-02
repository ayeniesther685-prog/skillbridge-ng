package org.skillbridge.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ProposalResponse(
        Long id,
        Long applicationId,
        String coverLetter,
        BigDecimal proposedPrice,
        Integer estimatedDeliveryDays,
        Instant createdAt,
        Instant updatedAt
) {}