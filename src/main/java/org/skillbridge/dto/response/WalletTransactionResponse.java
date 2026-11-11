package org.skillbridge.dto.response;

import org.skillbridge.enums.WalletTransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransactionResponse(
        Long id,
        WalletTransactionType type,
        Integer amountPoints,
        BigDecimal amountNaira,
        Integer balanceAfter,
        String reference,
        String description,
        Instant createdAt
) {}