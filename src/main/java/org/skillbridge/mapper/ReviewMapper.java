package org.skillbridge.mapper;

import org.skillbridge.dto.response.ReviewResponse;
import org.skillbridge.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getReviewer().getId(),
                (r.getReviewer().getFirstName() + " " + r.getReviewer().getLastName()).trim(),
                r.getReviewee().getId(),
                (r.getReviewee().getFirstName() + " " + r.getReviewee().getLastName()).trim(),
                r.getJob().getId(),
                r.getJob().getTitle(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}