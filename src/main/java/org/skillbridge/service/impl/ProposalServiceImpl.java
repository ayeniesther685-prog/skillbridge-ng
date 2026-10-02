package org.skillbridge.service.impl;

import org.skillbridge.dto.request.ProposalCreateRequest;
import org.skillbridge.dto.response.ProposalResponse;
import org.skillbridge.entity.Application;
import org.skillbridge.entity.Proposal;
import org.skillbridge.entity.User;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.ProposalMapper;
import org.skillbridge.repository.ApplicationRepository;
import org.skillbridge.repository.ProposalRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProposalServiceImpl {

    private final ProposalRepository proposalRepository;
    private final ApplicationRepository applicationRepository;
    private final ProposalMapper proposalMapper;
    private final CurrentUser currentUser;

    public ProposalServiceImpl(ProposalRepository proposalRepository,
                               ApplicationRepository applicationRepository,
                               ProposalMapper proposalMapper,
                               CurrentUser currentUser) {
        this.proposalRepository = proposalRepository;
        this.applicationRepository = applicationRepository;
        this.proposalMapper = proposalMapper;
        this.currentUser = currentUser;
    }

    public ProposalResponse findByApplication(Long applicationId) {
        Proposal proposal = proposalRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("No proposal for application " + applicationId));
        return proposalMapper.toResponse(proposal);
    }

    @Transactional
    public ProposalResponse update(Long applicationId, ProposalCreateRequest req) {
        User caller = currentUser.require();
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + applicationId));

        if (!app.getApplicant().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the applicant can update the proposal");
        }

        Proposal proposal = proposalRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("No proposal for this application"));

        proposal.setCoverLetter(req.coverLetter());
        proposal.setProposedPrice(req.proposedPrice());
        proposal.setEstimatedDeliveryDays(req.estimatedDeliveryDays());

        return proposalMapper.toResponse(proposalRepository.save(proposal));
    }
}