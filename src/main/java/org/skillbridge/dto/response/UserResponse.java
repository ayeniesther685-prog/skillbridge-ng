package org.skillbridge.dto.response;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profilePicture,
        String bio,
        String location,
        boolean active,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt
) {}