package org.skillbridge.repository;

import org.skillbridge.entity.ProviderVerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderVerificationDocumentRepository extends JpaRepository<ProviderVerificationDocument, Long> {

    List<ProviderVerificationDocument> findByVerificationId(Long verificationId);
}