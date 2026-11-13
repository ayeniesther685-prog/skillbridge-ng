package org.skillbridge.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AdminTreasuryResponse(
        Long treasuryUserId,
        Integer balancePoints,
        BigDecimal balanceNaira,
        BigDecimal conversionRate,
        long totalCommissionsRecorded,
        List<CommissionRecordResponse> recentCommissions
) {}