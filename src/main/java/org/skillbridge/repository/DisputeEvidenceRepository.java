package org.skillbridge.repository;

import org.skillbridge.entity.DisputeEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisputeEvidenceRepository extends JpaRepository<DisputeEvidence, Long> {

    List<DisputeEvidence> findByDisputeIdOrderByCreatedAtAsc(Long disputeId);

    long countByDisputeId(Long disputeId);
}
