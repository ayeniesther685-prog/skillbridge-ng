package org.skillbridge.dto.response;

public record AcademicDepartmentResponse(
        Long id, Long programId, String programName,
        String name, String code, String description
) {}