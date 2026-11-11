package org.skillbridge.repository;

import org.skillbridge.entity.AiQuotaTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiQuotaTierRepository extends JpaRepository<AiQuotaTier, Long> {
    Optional<AiQuotaTier> findByTierNameIgnoreCase(String tierName);
    List<AiQuotaTier> findByActiveTrueOrderByDailyLimitAsc();
    boolean existsByTierNameIgnoreCase(String tierName);
}