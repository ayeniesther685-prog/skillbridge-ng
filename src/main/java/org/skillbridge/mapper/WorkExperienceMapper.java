package org.skillbridge.mapper;

import org.skillbridge.dto.response.WorkExperienceResponse;
import org.skillbridge.entity.WorkExperience;
import org.springframework.stereotype.Component;

@Component
public class WorkExperienceMapper {
    public WorkExperienceResponse toResponse(WorkExperience w) {
        return new WorkExperienceResponse(
                w.getId(), w.getUser().getId(), w.getCompany(), w.getPosition(),
                w.getDescription(), w.getStartDate(), w.getEndDate(), w.isCurrent());
    }
}