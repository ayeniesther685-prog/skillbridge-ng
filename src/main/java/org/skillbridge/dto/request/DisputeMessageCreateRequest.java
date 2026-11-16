package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisputeMessageCreateRequest(
        @NotBlank @Size(max = 4000) String message
) {}
