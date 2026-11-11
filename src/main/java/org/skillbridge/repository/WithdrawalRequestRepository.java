package org.skillbridge.repository;

import org.skillbridge.entity.WithdrawalRequest;
import org.skillbridge.enums.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    Page<WithdrawalRequest> findByWalletIdOrderByRequestedAtDesc(Long walletId, Pageable pageable);
    Page<WithdrawalRequest> findByStatusOrderByRequestedAtDesc(WithdrawalStatus status, Pageable pageable);
    Page<WithdrawalRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);
}