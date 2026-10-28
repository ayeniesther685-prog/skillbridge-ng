package org.skillbridge.dto.response;

import org.skillbridge.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ContractResponse(
        Long id,
        Long jobId,
        String jobTitle,
        Long clientId,
        String clientName,
        Long freelancerId,
        String freelancerName,
        String title,
        String description,
        BigDecimal amount,
        LocalDate startDate,
        LocalDate endDate,
        ContractStatus status,
        List<MilestoneResponse> milestones,
        Instant createdAt,
        Instant updatedAt
) {}