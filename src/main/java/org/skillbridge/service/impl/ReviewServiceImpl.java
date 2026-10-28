package org.skillbridge.service.impl;

import org.skillbridge.dto.request.ReviewCreateRequest;
import org.skillbridge.dto.response.ReviewResponse;
import org.skillbridge.entity.Job;
import org.skillbridge.entity.Review;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ApplicationStatus;
import org.skillbridge.enums.ContractStatus;
import org.skillbridge.enums.NotificationType;
import org.skillbridge.exception.DuplicateResourceException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.ReviewMapper;
import org.skillbridge.repository.ApplicationRepository;
import org.skillbridge.repository.ContractRepository;
import org.skillbridge.repository.JobRepository;
import org.skillbridge.repository.ReviewRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl {

    private final ReviewRepository reviewRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ApplicationRepository applicationRepository;
    private final ReviewMapper reviewMapper;
    private final CurrentUser currentUser;
    private final NotificationServiceImpl notificationService;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             JobRepository jobRepository,
                             UserRepository userRepository,
                             ContractRepository contractRepository,
                             ApplicationRepository applicationRepository,
                             ReviewMapper reviewMapper,
                             CurrentUser currentUser,
                             NotificationServiceImpl notificationService) {
        this.reviewRepository = reviewRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.contractRepository = contractRepository;
        this.applicationRepository = applicationRepository;
        this.reviewMapper = reviewMapper;
        this.currentUser = currentUser;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReviewResponse create(ReviewCreateRequest req) {
        User me = currentUser.require();
        Job job = jobRepository.findById(req.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        User reviewee = userRepository.findById(req.revieweeId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (me.getId().equals(reviewee.getId())) {
            throw new IllegalArgumentException("You cannot review yourself");
        }
        if (reviewRepository.existsByReviewerIdAndJobId(me.getId(), job.getId())) {
            throw new DuplicateResourceException("You have already reviewed this job");
        }

        // Only participants of a completed contract can review.
        var contractOpt = contractRepository.findByJobId(job.getId());
        if (contractOpt.isEmpty() || contractOpt.get().getStatus() != ContractStatus.COMPLETED) {
            throw new IllegalStateException("Reviews are only allowed after a contract is COMPLETED");
        }
        var contract = contractOpt.get();
        boolean meIsParticipant = contract.getClient().getId().equals(me.getId())
                || contract.getFreelancer().getId().equals(me.getId());
        boolean revieweeIsParticipant = contract.getClient().getId().equals(reviewee.getId())
                || contract.getFreelancer().getId().equals(reviewee.getId());
        if (!meIsParticipant || !revieweeIsParticipant) {
            throw new AccessDeniedException("Both users must be participants of the job contract");
        }

        Review r = new Review();
        r.setReviewer(me);
        r.setReviewee(reviewee);
        r.setJob(job);
        r.setRating(req.rating());
        r.setComment(req.comment());
        Review saved = reviewRepository.save(r);

        notificationService.notify(
                reviewee,
                NotificationType.REVIEW,
                "New review",
                me.getFirstName() + " gave you a " + req.rating() + "-star review"
        );

        return reviewMapper.toResponse(saved);
    }

    public Page<ReviewResponse> listForUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        return reviewRepository.findByRevieweeId(userId, pageable).map(reviewMapper::toResponse);
    }

    public Page<ReviewResponse> listForJob(Long jobId, Pageable pageable) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found");
        }
        return reviewRepository.findByJobId(jobId, pageable).map(reviewMapper::toResponse);
    }

    public Page<ReviewResponse> listMine(Pageable pageable) {
        User me = currentUser.require();
        return reviewRepository.findByReviewerId(me.getId(), pageable).map(reviewMapper::toResponse);
    }

    public double averageForUser(Long userId) {
        Double avg = reviewRepository.averageRatingForUser(userId);
        return avg == null ? 0.0 : avg;
    }
}