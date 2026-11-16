package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin-only. `decision` is one of:
 *   RESOLVED_FOR_CLIENT
 *   RESOLVED_FOR_FREELANCER
 *   WITHDRAWN
 *   ESCALATED
 */
public record DisputeResolutionRequest(
        @NotBlank String decision,
        @NotBlank @Size(min = 20, max = 2000) String resolution
) {}
