package org.skillbridge.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.skillbridge.entity.Job;
import org.skillbridge.entity.Skill;
import org.skillbridge.enums.ExperienceLevel;
import org.skillbridge.enums.JobStatus;
import org.skillbridge.enums.JobType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class JobSpecification {

    private JobSpecification() {}

    public static Specification<Job> build(
            String search,
            String location,
            JobType jobType,
            ExperienceLevel experienceLevel,
            JobStatus status,
            BigDecimal minBudget,
            BigDecimal maxBudget,
            List<Long> skillIds) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"));
            }

            if (jobType != null) {
                predicates.add(cb.equal(root.get("jobType"), jobType));
            }

            if (experienceLevel != null) {
                predicates.add(cb.equal(root.get("experienceLevel"), experienceLevel));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (minBudget != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("budgetMax"), minBudget));
            }

            if (maxBudget != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("budgetMin"), maxBudget));
            }

            if (skillIds != null && !skillIds.isEmpty()) {
                Join<Job, Skill> skillsJoin = root.join("requiredSkills", JoinType.INNER);
                predicates.add(skillsJoin.get("id").in(skillIds));
                if (query != null) {
                    query.distinct(true);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}