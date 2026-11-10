package org.skillbridge.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssignmentSummaryResponse(
        Long id,
        Long courseId,
        String title,
        String description,
        Integer maxScore,
        BigDecimal weight,
        LocalDate dueDate,
        boolean published
) {}