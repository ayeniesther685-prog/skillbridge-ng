package org.skillbridge.mapper;

import org.skillbridge.dto.response.WalletResponse;
import org.skillbridge.dto.response.WalletTransactionResponse;
import org.skillbridge.dto.response.WithdrawalResponse;
import org.skillbridge.entity.Wallet;
import org.skillbridge.entity.WalletTransaction;
import org.skillbridge.entity.WithdrawalRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WalletMapper {

    public BigDecimal pointsToNaira(int points, BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(points));
    }

    public WalletResponse toWalletResponse(Wallet w, int pendingPoints, BigDecimal rate) {
        BigDecimal balanceNaira = pointsToNaira(w.getBalancePoints(), rate);
        BigDecimal pendingNaira = pointsToNaira(pendingPoints, rate);
        return new WalletResponse(
                w.getId(),
                w.getUser().getId(),
                w.getBalancePoints(),
                balanceNaira,
                rate,
                w.getCurrency(),
                pendingPoints,
                pendingNaira,
                w.getBalancePoints(),
                "Withdrawals are recorded and will be paid out when payment provider integration is live."
        );
    }

    public WalletTransactionResponse toTransactionResponse(WalletTransaction t, BigDecimal rate) {
        return new WalletTransactionResponse(
                t.getId(),
                t.getType(),
                t.getAmountPoints(),
                pointsToNaira(Math.abs(t.getAmountPoints()), rate),
                t.getBalanceAfter(),
                t.getReference(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }

    public WithdrawalResponse toWithdrawalResponse(WithdrawalRequest wr, BigDecimal rate) {
        return new WithdrawalResponse(
                wr.getId(),
                wr.getWallet().getId(),
                wr.getAmountPoints(),
                pointsToNaira(wr.getAmountPoints(), rate),
                wr.getStatus(),
                wr.getBankName(),
                wr.getAccountNumber(),
                wr.getAccountName(),
                wr.getAdminNotes(),
                wr.getRequestedAt(),
                wr.getProcessedAt(),
                wr.getStatus().name().equals("PAID")
                        ? "Paid out"
                        : "This withdrawal has been recorded. Processing requires payment provider integration (v2)."
        );
    }
}