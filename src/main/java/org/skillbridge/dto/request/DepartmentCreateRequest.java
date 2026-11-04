package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentCreateRequest(
        @NotNull Long programId,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 30) String code,
        @Size(max = 2000) String description
) {}