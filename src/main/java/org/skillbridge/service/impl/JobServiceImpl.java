package org.skillbridge.service.impl;

import org.skillbridge.dto.request.JobCreateRequest;
import org.skillbridge.dto.request.JobUpdateRequest;
import org.skillbridge.dto.response.JobResponse;
import org.skillbridge.entity.Job;
import org.skillbridge.entity.Skill;
import org.skillbridge.entity.User;
import org.skillbridge.enums.JobStatus;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.JobMapper;
import org.skillbridge.repository.JobRepository;
import org.skillbridge.repository.SkillRepository;
import org.skillbridge.repository.specification.JobSpecification;
import org.skillbridge.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class JobServiceImpl {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final JobMapper jobMapper;
    private final CurrentUser currentUser;

    public JobServiceImpl(JobRepository jobRepository,
                          SkillRepository skillRepository,
                          JobMapper jobMapper,
                          CurrentUser currentUser) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
        this.jobMapper = jobMapper;
        this.currentUser = currentUser;
    }

    public Page<JobResponse> search(String search,
                                    String location,
                                    org.skillbridge.enums.JobType jobType,
                                    org.skillbridge.enums.ExperienceLevel experienceLevel,
                                    JobStatus status,
                                    BigDecimal minBudget,
                                    BigDecimal maxBudget,
                                    List<Long> skillIds,
                                    Pageable pageable) {
        return jobRepository.findAll(
                JobSpecification.build(search, location, jobType, experienceLevel,
                        status, minBudget, maxBudget, skillIds),
                pageable
        ).map(jobMapper::toResponse);
    }

    public JobResponse findById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id));
        return jobMapper.toResponse(job);
    }

    @Transactional
    public JobResponse create(JobCreateRequest req) {
        validateBudget(req.budgetMin(), req.budgetMax());
        User client = currentUser.require();

        Job job = new Job();
        job.setClient(client);
        job.setTitle(req.title());
        job.setDescription(req.description());
        job.setBudgetMin(req.budgetMin());
        job.setBudgetMax(req.budgetMax());
        job.setLocation(req.location());
        job.setJobType(req.jobType());
        job.setExperienceLevel(req.experienceLevel());
        job.setDeadline(req.deadline());
        job.setStatus(JobStatus.OPEN);
        job.setRequiredSkills(loadSkills(req.skillIds()));

        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse update(Long id, JobUpdateRequest req) {
        validateBudget(req.budgetMin(), req.budgetMax());
        User caller = currentUser.require();
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id));

        if (!job.getClient().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the job owner can update this job");
        }

        job.setTitle(req.title());
        job.setDescription(req.description());
        job.setBudgetMin(req.budgetMin());
        job.setBudgetMax(req.budgetMax());
        job.setLocation(req.location());
        job.setJobType(req.jobType());
        job.setExperienceLevel(req.experienceLevel());
        job.setDeadline(req.deadline());
        job.setRequiredSkills(loadSkills(req.skillIds()));

        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse changeStatus(Long id, JobStatus newStatus) {
        User caller = currentUser.require();
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id));

        if (!job.getClient().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the job owner can change the status");
        }

        job.setStatus(newStatus);
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public void delete(Long id) {
        User caller = currentUser.require();
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id " + id));

        if (!job.getClient().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the job owner can delete this job");
        }

        jobRepository.delete(job);
    }

    private Set<Skill> loadSkills(Set<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Skill> skills = skillRepository.findAllById(skillIds);
        if (skills.size() != skillIds.size()) {
            throw new ResourceNotFoundException("One or more skills not found");
        }
        return new HashSet<>(skills);
    }

    private static void validateBudget(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("budgetMin must be <= budgetMax");
        }
    }
}