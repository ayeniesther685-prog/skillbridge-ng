package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminVerificationDecisionRequest(
        @NotBlank @Size(max = 2000) String reason
) {}