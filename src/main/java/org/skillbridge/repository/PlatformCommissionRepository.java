package org.skillbridge.repository;

import org.skillbridge.entity.PlatformCommission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformCommissionRepository extends JpaRepository<PlatformCommission, Long> {
    Page<PlatformCommission> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByStatus(String status);
    boolean existsByContractId(Long contractId);
}