package org.skillbridge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GradeSubmissionRequest(
        @NotNull @Min(0) Integer score,
        @Size(max = 4000) String feedback
) {}