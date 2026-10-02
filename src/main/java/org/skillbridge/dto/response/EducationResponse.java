package org.skillbridge.dto.response;

import java.time.LocalDate;

public record EducationResponse(
        Long id,
        Long userId,
        String institution,
        String degree,
        String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate,
        String description
) {}