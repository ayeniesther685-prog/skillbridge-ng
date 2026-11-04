package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotNull;

public record EnrollStudentRequest(
        @NotNull Long programId,
        Long departmentId
) {}