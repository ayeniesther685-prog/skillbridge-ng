package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Open a dispute on either a whole contract (milestoneId null) or a specific
 * milestone (milestoneId set). Caller must be a participant on the contract.
 */
public record OpenDisputeRequest(
        Long milestoneId,
        @NotBlank @Size(min = 20, max = 2000) String reason
) {}
