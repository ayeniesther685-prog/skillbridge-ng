package org.skillbridge.mapper;

import org.skillbridge.dto.response.ApplicationResponse;
import org.skillbridge.entity.Application;
import org.skillbridge.entity.Job;
import org.skillbridge.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    private final ProposalMapper proposalMapper;

    public ApplicationMapper(ProposalMapper proposalMapper) {
        this.proposalMapper = proposalMapper;
    }

    public ApplicationResponse toResponse(Application app) {
        Job job = app.getJob();
        User applicant = app.getApplicant();

        String applicantName = applicant == null
                ? null
                : (applicant.getFirstName() + " " + applicant.getLastName()).trim();

        return new ApplicationResponse(
                app.getId(),
                job == null ? null : job.getId(),
                job == null ? null : job.getTitle(),
                applicant == null ? null : applicant.getId(),
                applicantName,
                applicant == null ? null : applicant.getEmail(),
                app.getExpectedBudget(),
                app.getStatus(),
                proposalMapper.toResponse(app.getProposal()),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}