package org.skillbridge.dto.request;

import jakarta.validation.constraints.Size;

public record SubmissionCreateRequest(
        @Size(max = 20000) String content,
        @Size(max = 500) String fileUrl
) {}