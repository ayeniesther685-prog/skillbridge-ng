package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import org.skillbridge.enums.ApplicationStatus;

public record ApplicationStatusUpdateRequest(
        @NotNull ApplicationStatus status
) {}