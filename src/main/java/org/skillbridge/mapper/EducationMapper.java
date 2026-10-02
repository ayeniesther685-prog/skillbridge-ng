package org.skillbridge.mapper;

import org.skillbridge.dto.response.EducationResponse;
import org.skillbridge.entity.Education;
import org.springframework.stereotype.Component;

@Component
public class EducationMapper {
    public EducationResponse toResponse(Education e) {
        return new EducationResponse(
                e.getId(), e.getUser().getId(), e.getInstitution(), e.getDegree(),
                e.getFieldOfStudy(), e.getStartDate(), e.getEndDate(), e.getDescription());
    }
}