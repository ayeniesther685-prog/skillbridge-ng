package org.skillbridge.mapper;

import org.skillbridge.dto.response.ContractResponse;
import org.skillbridge.dto.response.MilestoneResponse;
import org.skillbridge.entity.Contract;
import org.skillbridge.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractMapper {

    private final MilestoneMapper milestoneMapper;

    public ContractMapper(MilestoneMapper milestoneMapper) {
        this.milestoneMapper = milestoneMapper;
    }

    public ContractResponse toResponse(Contract c) {
        User client = c.getClient();
        User freelancer = c.getFreelancer();

        List<MilestoneResponse> milestones = c.getMilestones().stream()
                .map(milestoneMapper::toResponse)
                .toList();

        return new ContractResponse(
                c.getId(),
                c.getJob().getId(),
                c.getJob().getTitle(),
                client.getId(),
                (client.getFirstName() + " " + client.getLastName()).trim(),
                freelancer.getId(),
                (freelancer.getFirstName() + " " + freelancer.getLastName()).trim(),
                c.getTitle(),
                c.getDescription(),
                c.getAmount(),
                c.getStartDate(),
                c.getEndDate(),
                c.getStatus(),
                milestones,
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}