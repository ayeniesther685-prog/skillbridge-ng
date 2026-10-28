package org.skillbridge.repository;

import org.skillbridge.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByContractIdOrderByCreatedAtAsc(Long contractId);

    long countByContractId(Long contractId);
}