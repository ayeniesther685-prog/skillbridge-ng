package org.skillbridge.dto.response;

public record AcademicProgramResponse(
        Long id, String name, String code, String description,
        Integer durationMonths, boolean active
) {}