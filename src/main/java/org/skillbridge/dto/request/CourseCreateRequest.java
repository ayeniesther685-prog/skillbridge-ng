package org.skillbridge.dto.request;

import jakarta.validation.constraints.*;
import org.skillbridge.enums.AcademicLevel;

public record CourseCreateRequest(
        @NotNull Long departmentId,
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 4000) String description,
        @NotNull AcademicLevel level,
        @Min(1) @Max(12) Integer credits,
        @Positive Integer durationHours
) {}