package org.skillbridge.mapper;

import org.skillbridge.dto.response.ProfileResponse;
import org.skillbridge.entity.Profile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {
    public ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(
                p.getId(), p.getUser().getId(), p.getHeadline(), p.getWebsite(),
                p.getLinkedinUrl(), p.getGithubUrl(), p.getTwitterUrl(),
                p.getAvailability(), p.getHourlyRate(), p.getCreatedAt(), p.getUpdatedAt());
    }
}