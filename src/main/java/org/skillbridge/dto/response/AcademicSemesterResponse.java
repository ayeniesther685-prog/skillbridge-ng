package org.skillbridge.dto.response;

import java.time.LocalDate;

public record AcademicSemesterResponse(
        Long id, Long sessionId, String sessionName,
        String name, LocalDate startDate, LocalDate endDate
) {}