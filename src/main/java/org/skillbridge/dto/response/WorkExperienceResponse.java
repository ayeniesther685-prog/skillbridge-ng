package org.skillbridge.dto.response;

import java.time.LocalDate;

public record WorkExperienceResponse(
        Long id,
        Long userId,
        String company,
        String position,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean current
) {}