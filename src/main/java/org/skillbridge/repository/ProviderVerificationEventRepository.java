package org.skillbridge.repository;

import org.skillbridge.entity.ProviderVerificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderVerificationEventRepository extends JpaRepository<ProviderVerificationEvent, Long> {

    List<ProviderVerificationEvent> findByVerificationIdOrderByCreatedAtDesc(Long verificationId);
}