package org.skillbridge.dto.request;

import jakarta.validation.constraints.*;

public record ReviewCreateRequest(
        @NotNull Long jobId,
        @NotNull Long revieweeId,
        @NotNull @Min(1) @Max(5) Short rating,
        @Size(max = 2000) String comment
) {}