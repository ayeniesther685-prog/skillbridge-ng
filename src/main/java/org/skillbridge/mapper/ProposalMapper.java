package org.skillbridge.mapper;

import org.skillbridge.dto.response.ProposalResponse;
import org.skillbridge.entity.Proposal;
import org.springframework.stereotype.Component;

@Component
public class ProposalMapper {

    public ProposalResponse toResponse(Proposal proposal) {
        if (proposal == null) return null;
        return new ProposalResponse(
                proposal.getId(),
                proposal.getApplication().getId(),
                proposal.getCoverLetter(),
                proposal.getProposedPrice(),
                proposal.getEstimatedDeliveryDays(),
                proposal.getCreatedAt(),
                proposal.getUpdatedAt()
        );
    }
}