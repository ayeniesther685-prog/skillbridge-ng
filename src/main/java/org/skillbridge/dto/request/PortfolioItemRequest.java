package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PortfolioItemRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @Size(max = 500) String imageUrl,
        @Size(max = 500) String projectUrl,
        @Size(max = 500) String technologies
) {}