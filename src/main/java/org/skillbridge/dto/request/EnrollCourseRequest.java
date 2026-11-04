package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotNull;

public record EnrollCourseRequest(
        @NotNull Long courseId,
        @NotNull Long semesterId
) {}