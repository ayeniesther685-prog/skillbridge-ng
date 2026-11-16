package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Client-side request to reopen a submitted milestone with a revision.
 *
 * If `isNewRequirements` is true, the client is explicitly acknowledging that
 * the request is for scope not covered by the original milestone. In that
 * case the request is preserved as an audit record (status
 * ESCALATED_AS_NEW_REQUIREMENT) and does NOT consume a revision slot — the
 * client must instead create a new milestone.
 */
public record RevisionRequestCreateRequest(
        @NotBlank @Size(max = 2000) String reason,
        boolean isNewRequirements
) {}
