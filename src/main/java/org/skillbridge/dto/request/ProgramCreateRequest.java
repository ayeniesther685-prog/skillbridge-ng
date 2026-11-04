package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProgramCreateRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 30) String code,
        @Size(max = 2000) String description,
        @Positive Integer durationMonths
) {}