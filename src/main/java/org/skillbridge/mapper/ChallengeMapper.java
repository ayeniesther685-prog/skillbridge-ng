package org.skillbridge.mapper;

import org.skillbridge.dto.response.ChallengeResponse;
import org.skillbridge.dto.response.ChallengeSubmissionResponse;
import org.skillbridge.entity.ChallengeSubmission;
import org.skillbridge.entity.DailyChallenge;
import org.skillbridge.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ChallengeMapper {

    public ChallengeResponse toResponse(DailyChallenge c, long submissionCount) {
        return new ChallengeResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getPointReward(),
                c.getDifficulty(),
                c.getMaxWinners(),
                c.getWinnersCount(),
                c.getActiveFrom(),
                c.getActiveUntil(),
                c.isActive(),
                submissionCount
        );
    }

    public ChallengeSubmissionResponse toSubmissionResponse(ChallengeSubmission s) {
        User u = s.getUser();
        String userName = (u.getFirstName() + " " + u.getLastName()).trim();
        return new ChallengeSubmissionResponse(
                s.getId(),
                s.getChallenge().getId(),
                s.getChallenge().getTitle(),
                s.getUser().getId(),
                userName,
                s.getContent(),
                s.getStatus(),
                s.getPointsAwarded(),
                s.getAdminFeedback(),
                s.getCreatedAt(),
                s.getReviewedAt()
        );
    }
}