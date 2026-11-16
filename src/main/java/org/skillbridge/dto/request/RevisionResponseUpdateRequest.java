package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Freelancer-side response to an OPEN revision request.
 *
 * `accept = true`  -> status ACCEPTED, milestone goes back to IN_PROGRESS.
 * `accept = false` -> status REJECTED, milestone stays SUBMITTED (client can
 *                     approve as-is, or escalate as new requirements).
 */
public record RevisionResponseUpdateRequest(
        @NotNull Boolean accept,
        @NotBlank @Size(max = 2000) String response
) {}
