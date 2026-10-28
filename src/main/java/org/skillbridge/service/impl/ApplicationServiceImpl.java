package org.skillbridge.service.impl;

import org.skillbridge.dto.request.ApplicationCreateRequest;
import org.skillbridge.dto.response.ApplicationResponse;
import org.skillbridge.entity.Application;
import org.skillbridge.entity.Job;
import org.skillbridge.entity.Proposal;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ApplicationStatus;
import org.skillbridge.enums.JobStatus;
import org.skillbridge.exception.DuplicateResourceException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.ApplicationMapper;
import org.skillbridge.repository.ApplicationRepository;
import org.skillbridge.repository.JobRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.skillbridge.enums.RoleName;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ApplicationServiceImpl {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ApplicationMapper applicationMapper;
    private final CurrentUser currentUser;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  ApplicationMapper applicationMapper,
                                  CurrentUser currentUser) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.applicationMapper = applicationMapper;
        this.currentUser = currentUser;
    }

       @Transactional
    public ApplicationResponse apply(Long jobId, ApplicationCreateRequest req) {
        User applicant = currentUser.require();

        boolean isClient = applicant.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.CLIENT);
        if (isClient) {
            throw new AccessDeniedException("CLIENT accounts cannot apply to jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + jobId));

        if (job.getClient().getId().equals(applicant.getId())) {
            throw new IllegalArgumentException("You cannot apply to your own job");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException("This job is no longer accepting applications");
        }
        if (applicationRepository.existsByJobIdAndApplicantId(jobId, applicant.getId())) {
            throw new DuplicateResourceException("You have already applied to this job");
        }

        Application app = new Application();
        app.setJob(job);
        app.setApplicant(applicant);
        app.setExpectedBudget(req.expectedBudget());
        app.setStatus(ApplicationStatus.PENDING);

        Proposal proposal = new Proposal();
        proposal.setApplication(app);
        proposal.setCoverLetter(req.proposal().coverLetter());
        proposal.setProposedPrice(req.proposal().proposedPrice());
        proposal.setEstimatedDeliveryDays(req.proposal().estimatedDeliveryDays());
        app.setProposal(proposal);

        return applicationMapper.toResponse(applicationRepository.save(app));
    }
    public ApplicationResponse findById(Long id) {
        Application app = loadAndAuthorizeRead(id);
        return applicationMapper.toResponse(app);
    }

    public Page<ApplicationResponse> listMine(Pageable pageable) {
        User caller = currentUser.require();
        // Applications I submitted OR applications to jobs I posted
        // Simple version: show applications I submitted
        return applicationRepository.findByApplicantId(caller.getId(), pageable)
                .map(applicationMapper::toResponse);
    }

    public Page<ApplicationResponse> listForJob(Long jobId, Pageable pageable) {
        User caller = currentUser.require();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + jobId));
        if (!job.getClient().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the job owner can view applications for this job");
        }
        return applicationRepository.findByJobId(jobId, pageable)
                .map(applicationMapper::toResponse);
    }

    @Transactional
    public ApplicationResponse changeStatus(Long id, ApplicationStatus newStatus) {
        User caller = currentUser.require();
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));

        if (!app.getJob().getClient().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the job owner can change application status");
        }

        ApplicationStatus current = app.getStatus();
        if (!current.allowedNext().contains(newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + current + " -> " + newStatus);
        }

        app.setStatus(newStatus);
        return applicationMapper.toResponse(applicationRepository.save(app));
    }
     @Transactional
    public ApplicationResponse withdraw(Long id) {
        User caller = currentUser.require();
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));

        if (!app.getApplicant().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the applicant can withdraw this application");
        }

        ApplicationStatus current = app.getStatus();
        if (!current.allowedNext().contains(ApplicationStatus.WITHDRAWN)) {
            throw new IllegalStateException(
                    "Cannot withdraw an application in status " + current);
        }


   
        app.setStatus(ApplicationStatus.WITHDRAWN);
        return applicationMapper.toResponse(applicationRepository.save(app));
    }

    @Transactional
    public void delete(Long id) {
        User caller = currentUser.require();
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));

        boolean isApplicant = app.getApplicant().getId().equals(caller.getId());
        boolean isClient = app.getJob().getClient().getId().equals(caller.getId());
        if (!isApplicant && !isClient) {
            throw new AccessDeniedException("Not your application to delete");
        }

        applicationRepository.delete(app);
    }

    private Application loadAndAuthorizeRead(Long id) {
        User caller = currentUser.require();
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));

        boolean isApplicant = app.getApplicant().getId().equals(caller.getId());
        boolean isClient = app.getJob().getClient().getId().equals(caller.getId());
        if (!isApplicant && !isClient) {
            throw new AccessDeniedException("Not your application to view");
        }
        return app;
    }
}