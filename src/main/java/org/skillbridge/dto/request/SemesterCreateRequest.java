package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SemesterCreateRequest(
        @NotNull Long sessionId,
        @NotBlank @Size(max = 50) String name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}