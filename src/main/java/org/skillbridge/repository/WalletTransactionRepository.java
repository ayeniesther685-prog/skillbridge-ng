package org.skillbridge.repository;

import org.skillbridge.entity.WalletTransaction;
import org.skillbridge.enums.WalletTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    Page<WalletTransaction> findByWalletIdAndTypeOrderByCreatedAtDesc(
            Long walletId, WalletTransactionType type, Pageable pageable);
    long countByWalletId(Long walletId);
}