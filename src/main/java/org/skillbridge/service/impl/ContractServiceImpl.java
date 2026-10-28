package org.skillbridge.service.impl;

import org.skillbridge.dto.request.ContractCreateRequest;
import org.skillbridge.dto.response.ContractResponse;
import org.skillbridge.entity.Application;
import org.skillbridge.entity.Contract;
import org.skillbridge.entity.Job;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ApplicationStatus;
import org.skillbridge.enums.ContractStatus;
import org.skillbridge.enums.JobStatus;
import org.skillbridge.enums.NotificationType;
import org.skillbridge.exception.DuplicateResourceException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.ContractMapper;
import org.skillbridge.repository.ApplicationRepository;
import org.skillbridge.repository.ContractRepository;
import org.skillbridge.repository.JobRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class ContractServiceImpl {

    private final ContractRepository contractRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ContractMapper contractMapper;
    private final CurrentUser currentUser;
    private final NotificationServiceImpl notificationService;

    public ContractServiceImpl(ContractRepository contractRepository,
                               ApplicationRepository applicationRepository,
                               JobRepository jobRepository,
                               ContractMapper contractMapper,
                               CurrentUser currentUser,
                               NotificationServiceImpl notificationService) {
        this.contractRepository = contractRepository;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.contractMapper = contractMapper;
        this.currentUser = currentUser;
        this.notificationService = notificationService;
    }

    @Transactional
    public ContractResponse createFromApplication(ContractCreateRequest req) {
        User caller = currentUser.require();
        Application application = applicationRepository.findById(req.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        Job job = application.getJob();
        if (!job.getClient().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the job owner can create a contract");
        }
        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new IllegalStateException("Application must be ACCEPTED to create a contract");
        }
        if (contractRepository.existsByJobId(job.getId())) {
            throw new DuplicateResourceException("A contract already exists for this job");
        }

        BigDecimal amount = application.getProposal() != null
                ? application.getProposal().getProposedPrice()
                : (job.getBudgetMax() != null ? job.getBudgetMax() : BigDecimal.ZERO);

        Contract contract = new Contract();
        contract.setJob(job);
        contract.setClient(job.getClient());
        contract.setFreelancer(application.getApplicant());
        contract.setTitle(job.getTitle());
        contract.setDescription(job.getDescription());
        contract.setAmount(amount);
        contract.setStatus(ContractStatus.ACTIVE);

        Contract saved = contractRepository.save(contract);

        // Flip the job to IN_PROGRESS
        job.setStatus(JobStatus.IN_PROGRESS);
        jobRepository.save(job);

        // Notify freelancer
        notificationService.notify(
                saved.getFreelancer(),
                NotificationType.CONTRACT,
                "New contract created",
                "A contract was created for job: " + job.getTitle()
        );

        return contractMapper.toResponse(saved);
    }

    public Page<ContractResponse> listMine(Pageable pageable) {
        User me = currentUser.require();
        // Show contracts where I am either client or freelancer.
        // We use client page since ordering by createdAt is consistent on both.
        // Merge would need a union query; for now, show client-side first.
        Page<Contract> asClient = contractRepository.findByClientId(me.getId(), pageable);
        if (asClient.hasContent()) {
            return asClient.map(contractMapper::toResponse);
        }
        return contractRepository.findByFreelancerId(me.getId(), pageable)
                .map(contractMapper::toResponse);
    }

    public ContractResponse findById(Long id) {
        User me = currentUser.require();
        Contract c = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + id));
        boolean isClient = c.getClient().getId().equals(me.getId());
        boolean isFreelancer = c.getFreelancer().getId().equals(me.getId());
        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException("Not your contract");
        }
        return contractMapper.toResponse(c);
    }

    @Transactional
    public ContractResponse changeStatus(Long id, ContractStatus newStatus) {
        User me = currentUser.require();
        Contract c = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + id));

        boolean isClient = c.getClient().getId().equals(me.getId());
        boolean isFreelancer = c.getFreelancer().getId().equals(me.getId());
        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException("Not your contract");
        }

        // Simple transition rules
        ContractStatus current = c.getStatus();
        if (current == ContractStatus.COMPLETED || current == ContractStatus.CANCELLED) {
            throw new IllegalStateException("Contract is in terminal status " + current);
        }
        if (newStatus == ContractStatus.CANCELLED && !isClient) {
            throw new AccessDeniedException("Only the client can cancel a contract");
        }

        c.setStatus(newStatus);
        Contract saved = contractRepository.save(c);

        notificationService.notify(
                isClient ? c.getFreelancer() : c.getClient(),
                NotificationType.CONTRACT,
                "Contract status changed",
                "Contract for '" + c.getTitle() + "' is now " + newStatus
        );

        return contractMapper.toResponse(saved);
    }
}