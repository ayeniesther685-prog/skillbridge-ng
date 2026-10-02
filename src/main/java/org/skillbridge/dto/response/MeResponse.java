package org.skillbridge.dto.response;

import java.util.Set;

public record MeResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profilePicture,
        String bio,
        String location,
        boolean active,
        Set<String> roles
) {}