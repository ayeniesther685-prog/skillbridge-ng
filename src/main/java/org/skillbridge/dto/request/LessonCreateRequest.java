package org.skillbridge.dto.request;

import jakarta.validation.constraints.*;

public record LessonCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
        @NotNull @Min(1) Integer orderNumber,
        @Min(1) Integer durationMinutes,
        @Size(max = 2000) String learningObjectives,
        @Size(max = 500) String videoUrl
) {}