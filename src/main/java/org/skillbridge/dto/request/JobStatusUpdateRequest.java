package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import org.skillbridge.enums.JobStatus;

public record JobStatusUpdateRequest(
        @NotNull JobStatus status
) {}