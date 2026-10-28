package org.skillbridge.dto.request;

import jakarta.validation.constraints.NotNull;

public record ConversationCreateRequest(
        @NotNull Long otherUserId
) {}