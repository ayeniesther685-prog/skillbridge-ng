package org.skillbridge.mapper;

import org.skillbridge.dto.response.JobResponse;
import org.skillbridge.dto.response.SkillResponse;
import org.skillbridge.entity.Job;
import org.skillbridge.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobMapper {

    private final SkillMapper skillMapper;

    public JobMapper(SkillMapper skillMapper) {
        this.skillMapper = skillMapper;
    }

    public JobResponse toResponse(Job job) {
        User client = job.getClient();
        String clientName = client == null
                ? null
                : (client.getFirstName() + " " + client.getLastName()).trim();

        List<SkillResponse> skills = job.getRequiredSkills().stream()
                .map(skillMapper::toResponse)
                .toList();

        return new JobResponse(
                job.getId(),
                client == null ? null : client.getId(),
                clientName,
                job.getTitle(),
                job.getDescription(),
                job.getBudgetMin(),
                job.getBudgetMax(),
                job.getLocation(),
                job.getJobType(),
                job.getExperienceLevel(),
                job.getStatus(),
                job.getDeadline(),
                skills,
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}