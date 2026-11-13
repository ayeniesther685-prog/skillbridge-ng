package org.skillbridge.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record CommissionRecordResponse(
        Long id,
        Long contractId,
        String contractTitle,
        Integer amountPoints,
        BigDecimal amountNaira,
        BigDecimal feePercent,
        Long freelancerId,
        String freelancerName,
        String status,
        Instant createdAt
) {}