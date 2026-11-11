package org.skillbridge.dto.response;

import org.skillbridge.enums.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record WithdrawalResponse(
        Long id,
        Long walletId,
        Integer amountPoints,
        BigDecimal amountNaira,
        WithdrawalStatus status,
        String bankName,
        String accountNumber,
        String accountName,
        String adminNotes,
        Instant requestedAt,
        Instant processedAt,
        String note
) {}