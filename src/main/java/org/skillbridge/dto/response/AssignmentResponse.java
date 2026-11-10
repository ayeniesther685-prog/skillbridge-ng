package org.skillbridge.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record AssignmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        String description,
        String instructions,
        Integer maxScore,
        BigDecimal weight,
        LocalDate dueDate,
        boolean published,
        Instant createdAt,
        Instant updatedAt
) {}