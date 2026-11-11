package org.skillbridge.dto.response;

import java.math.BigDecimal;

public record WalletResponse(
        Long walletId,
        Long userId,
        Integer balancePoints,
        BigDecimal balanceNaira,
        BigDecimal conversionRate,
        String currency,
        Integer pendingWithdrawalPoints,
        BigDecimal pendingWithdrawalNaira,
        Integer availableForWithdrawalPoints,
        String withdrawableNote
) {}