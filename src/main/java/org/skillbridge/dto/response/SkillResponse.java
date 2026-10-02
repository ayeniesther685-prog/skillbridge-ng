package org.skillbridge.dto.response;

public record SkillResponse(
        Long id,
        String name,
        String description,
        String category
) {}