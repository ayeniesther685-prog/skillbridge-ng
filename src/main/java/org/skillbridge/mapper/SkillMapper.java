package org.skillbridge.mapper;

import org.skillbridge.dto.response.SkillResponse;
import org.skillbridge.entity.Skill;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {
    public SkillResponse toResponse(Skill s) {
        return new SkillResponse(s.getId(), s.getName(), s.getDescription(), s.getCategory());
    }
}