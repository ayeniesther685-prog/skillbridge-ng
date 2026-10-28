package org.skillbridge.repository;

import org.skillbridge.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByReviewerIdAndJobId(Long reviewerId, Long jobId);

    Page<Review> findByRevieweeId(Long revieweeId, Pageable pageable);

    Page<Review> findByJobId(Long jobId, Pageable pageable);

    Page<Review> findByReviewerId(Long reviewerId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.reviewee.id = :userId")
    Double averageRatingForUser(@Param("userId") Long userId);

    long countByRevieweeId(Long userId);
}