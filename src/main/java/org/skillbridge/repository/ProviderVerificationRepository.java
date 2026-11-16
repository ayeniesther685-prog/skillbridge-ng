package org.skillbridge.repository;

import org.skillbridge.entity.ProviderVerification;
import org.skillbridge.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderVerificationRepository extends JpaRepository<ProviderVerification, Long> {

    Optional<ProviderVerification> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Page<ProviderVerification> findByStatus(VerificationStatus status, Pageable pageable);

    Page<ProviderVerification> findByStatusAndProviderType(
            VerificationStatus status,
            org.skillbridge.enums.ProviderType providerType,
            Pageable pageable
    );
}