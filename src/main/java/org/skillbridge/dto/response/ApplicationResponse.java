package org.skillbridge.dto.response;

import org.skillbridge.enums.ApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        Long applicantId,
        String applicantName,
        String applicantEmail,
        BigDecimal expectedBudget,
        ApplicationStatus status,
        ProposalResponse proposal,
        Instant createdAt,
        Instant updatedAt
) {}