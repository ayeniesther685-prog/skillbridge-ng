package org.skillbridge.repository;

import org.skillbridge.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    Optional<Proposal> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);
}