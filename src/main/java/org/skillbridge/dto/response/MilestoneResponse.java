package org.skillbridge.dto.response;

import org.skillbridge.enums.MilestoneStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record MilestoneResponse(
        Long id,
        Long contractId,
        String title,
        String description,
        BigDecimal amount,
        LocalDate dueDate,
        MilestoneStatus status,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {}