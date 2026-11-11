package org.skillbridge.repository;

import org.skillbridge.entity.AiUsageEvent;
import org.skillbridge.enums.AiUsageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AiUsageEventRepository extends JpaRepository<AiUsageEvent, Long> {
    Page<AiUsageEvent> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<AiUsageEvent> findByUserIdAndCreatedAtAfter(Long userId, Instant since);
    long countByUserIdAndStatusAndCreatedAtAfter(Long userId, AiUsageStatus status, Instant since);
}