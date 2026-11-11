package org.skillbridge.service.impl;

import org.skillbridge.dto.request.ChallengeSubmitRequest;
import org.skillbridge.dto.response.ChallengeResponse;
import org.skillbridge.dto.response.ChallengeSubmissionResponse;
import org.skillbridge.entity.ChallengeSubmission;
import org.skillbridge.entity.DailyChallenge;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ChallengeSubmissionStatus;
import org.skillbridge.exception.DuplicateResourceException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.ChallengeMapper;
import org.skillbridge.repository.ChallengeSubmissionRepository;
import org.skillbridge.repository.DailyChallengeRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChallengeServiceImpl {

    private final DailyChallengeRepository challengeRepository;
    private final ChallengeSubmissionRepository submissionRepository;
    private final ChallengeMapper mapper;
    private final CurrentUser currentUser;

    public ChallengeServiceImpl(DailyChallengeRepository challengeRepository,
                                ChallengeSubmissionRepository submissionRepository,
                                ChallengeMapper mapper,
                                CurrentUser currentUser) {
        this.challengeRepository = challengeRepository;
        this.submissionRepository = submissionRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public List<ChallengeResponse> listActive() {
        return challengeRepository.findByActiveTrueAndActiveUntilAfterOrderByCreatedAtDesc(Instant.now())
                .stream()
                .map(c -> mapper.toResponse(c, countSubmissions(c.getId())))
                .toList();
    }

    public ChallengeResponse getById(Long id) {
        DailyChallenge c = challengeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));
        return mapper.toResponse(c, countSubmissions(c.getId()));
    }

    @Transactional
    public ChallengeSubmissionResponse submit(Long challengeId, ChallengeSubmitRequest req) {
        User me = currentUser.require();
        DailyChallenge c = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found"));

        if (!c.isActive() || c.getActiveUntil().isBefore(Instant.now())) {
            throw new IllegalStateException("This challenge is no longer active");
        }
        if (c.getMaxWinners() != null && c.getWinnersCount() >= c.getMaxWinners()) {
            throw new IllegalStateException("This challenge has reached its winner limit");
        }
        if (submissionRepository.existsByChallengeIdAndUserId(challengeId, me.getId())) {
            throw new DuplicateResourceException("You have already submitted this challenge");
        }

        ChallengeSubmission s = new ChallengeSubmission();
        s.setChallenge(c);
        s.setUser(me);
        s.setContent(req.content());
        s.setStatus(ChallengeSubmissionStatus.SUBMITTED);
        return mapper.toSubmissionResponse(submissionRepository.save(s));
    }

    public List<ChallengeSubmissionResponse> mySubmissions() {
        User me = currentUser.require();
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(me.getId())
                .stream().map(mapper::toSubmissionResponse).toList();
    }

    private long countSubmissions(Long challengeId) {
        return submissionRepository.countByChallengeIdAndStatus(
                challengeId, ChallengeSubmissionStatus.SUBMITTED)
                + submissionRepository.countByChallengeIdAndStatus(
                challengeId, ChallengeSubmissionStatus.ACCEPTED);
    }
}