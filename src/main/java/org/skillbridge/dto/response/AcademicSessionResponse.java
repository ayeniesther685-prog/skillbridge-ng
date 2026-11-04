package org.skillbridge.dto.response;

import java.time.LocalDate;

public record AcademicSessionResponse(
        Long id, String name, LocalDate startDate, LocalDate endDate, boolean current
) {}