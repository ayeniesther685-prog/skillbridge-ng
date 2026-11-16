package org.skillbridge.mapper;

import org.skillbridge.dto.response.MilestoneResponse;
import org.skillbridge.entity.Milestone;
import org.springframework.stereotype.Component;

@Component
public class MilestoneMapper {

    public MilestoneResponse toResponse(Milestone m) {
        return new MilestoneResponse(
                m.getId(),
                m.getContract().getId(),
                m.getTitle(),
                m.getDescription(),
                m.getAmount(),
                m.getDueDate(),
                m.getStatus(),
                m.getRevisionLimit() != null ? m.getRevisionLimit() : 3,
                m.getRevisionCount() != null ? m.getRevisionCount() : 0,
                m.getSubmittedAt(),
                m.getApprovedAt(),
                m.getReviewDeadlineAt(),
                m.getReleasedAt(),
                m.getCompletedAt(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}
