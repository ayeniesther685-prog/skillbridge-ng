package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import org.skillbridge.enums.LessonProgressStatus;

public record ProgressUpdateRequest(
        @NotNull LessonProgressStatus status
) {}