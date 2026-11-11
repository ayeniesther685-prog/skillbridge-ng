package org.skillbridge.service.impl;

import org.skillbridge.config.WalletProperties;
import org.skillbridge.dto.request.WithdrawalRequestCreateRequest;
import org.skillbridge.dto.response.WalletResponse;
import org.skillbridge.dto.response.WalletTransactionResponse;
import org.skillbridge.dto.response.WithdrawalResponse;
import org.skillbridge.entity.User;
import org.skillbridge.entity.Wallet;
import org.skillbridge.entity.WalletTransaction;
import org.skillbridge.entity.WithdrawalRequest;
import org.skillbridge.enums.WalletTransactionType;
import org.skillbridge.enums.WithdrawalStatus;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.WalletMapper;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.repository.WalletRepository;
import org.skillbridge.repository.WalletTransactionRepository;
import org.skillbridge.repository.WithdrawalRequestRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WalletServiceImpl {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository txRepository;
    private final WithdrawalRequestRepository withdrawalRepository;
    private final WalletMapper mapper;
    private final WalletProperties props;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public WalletServiceImpl(WalletRepository walletRepository,
                             WalletTransactionRepository txRepository,
                             WithdrawalRequestRepository withdrawalRepository,
                             WalletMapper mapper,
                             WalletProperties props,
                             UserRepository userRepository,
                             CurrentUser currentUser) {
        this.walletRepository = walletRepository;
        this.txRepository = txRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.mapper = mapper;
        this.props = props;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public WalletResponse getMyWallet() {
        User me = currentUser.require();
        Wallet wallet = loadOrCreate(me);
        int pending = pendingWithdrawalPoints(wallet.getId());
        return mapper.toWalletResponse(wallet, pending, props.getPointsToNairaRate());
    }

    public Page<WalletTransactionResponse> getMyTransactions(Pageable pageable) {
        User me = currentUser.require();
        Wallet wallet = loadOrCreate(me);
        return txRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable)
                .map(t -> mapper.toTransactionResponse(t, props.getPointsToNairaRate()));
    }

    @Transactional
    public WithdrawalResponse requestWithdrawal(WithdrawalRequestCreateRequest req) {
        User me = currentUser.require();
        Wallet wallet = loadOrCreate(me);

        int pending = pendingWithdrawalPoints(wallet.getId());
        if (pending + req.amountPoints() > wallet.getBalancePoints()) {
            throw new IllegalArgumentException(
                    "Insufficient balance. You have " + wallet.getBalancePoints()
                            + " points, with " + pending + " already in pending withdrawals.");
        }

        WithdrawalRequest wr = new WithdrawalRequest();
        wr.setWallet(wallet);
        wr.setAmountPoints(req.amountPoints());
        wr.setStatus(WithdrawalStatus.PENDING);
        wr.setBankName(req.bankName());
        wr.setAccountNumber(req.accountNumber());
        wr.setAccountName(req.accountName());
        WithdrawalRequest saved = withdrawalRepository.save(wr);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setType(WalletTransactionType.WITHDRAWAL_REQUESTED);
        tx.setAmountPoints(0);
        tx.setBalanceAfter(wallet.getBalancePoints());
        tx.setReference("WITHDRAWAL-" + saved.getId());
        tx.setDescription("Withdrawal request for " + req.amountPoints()
                + " points — PENDING (points not yet deducted)");
        txRepository.save(tx);

        return mapper.toWithdrawalResponse(saved, props.getPointsToNairaRate());
    }

    public Page<WithdrawalResponse> getMyWithdrawals(Pageable pageable) {
        User me = currentUser.require();
        Wallet wallet = loadOrCreate(me);
        return withdrawalRepository.findByWalletIdOrderByRequestedAtDesc(wallet.getId(), pageable)
                .map(w -> mapper.toWithdrawalResponse(w, props.getPointsToNairaRate()));
    }

    @Transactional
    public void creditPoints(Long userId, int points, WalletTransactionType type,
                             String reference, String description) {
        if (points <= 0) return;
        Wallet wallet = loadOrCreateById(userId);
        int newBalance = wallet.getBalancePoints() + points;
        wallet.setBalancePoints(newBalance);
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setType(type);
        tx.setAmountPoints(points);
        tx.setBalanceAfter(newBalance);
        tx.setReference(reference);
        tx.setDescription(description);
        txRepository.save(tx);
    }

    private Wallet loadOrCreate(User user) {
        return walletRepository.findByUserId(user.getId()).orElseGet(() -> {
            Wallet w = new Wallet();
            w.setUser(user);
            w.setBalancePoints(0);
            w.setCurrency(props.getCurrency());
            return walletRepository.save(w);
        });
    }

    private Wallet loadOrCreateById(Long userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            Wallet w = new Wallet();
            w.setUser(u);
            w.setBalancePoints(0);
            w.setCurrency(props.getCurrency());
            return walletRepository.save(w);
        });
    }

    private int pendingWithdrawalPoints(Long walletId) {
        return (int) withdrawalRepository
                .findByWalletIdOrderByRequestedAtDesc(walletId, Pageable.unpaged())
                .stream()
                .filter(w -> w.getStatus() == WithdrawalStatus.PENDING
                        || w.getStatus() == WithdrawalStatus.APPROVED)
                .mapToInt(WithdrawalRequest::getAmountPoints)
                .sum();
    }
}