package org.skillbridge.mapper;

import org.skillbridge.dto.response.ProviderVerificationEventResponse;
import org.skillbridge.dto.response.ProviderVerificationResponse;
import org.skillbridge.entity.ProviderVerification;
import org.skillbridge.entity.ProviderVerificationEvent;
import org.springframework.stereotype.Component;

@Component
public class ProviderVerificationMapper {

    public ProviderVerificationResponse toResponse(ProviderVerification pv) {
        return new ProviderVerificationResponse(
                pv.getId(),
                pv.getUser().getId(),
                pv.getProviderType(),
                pv.getLegalName(),
                pv.getCacRegistrationNumber(),
                pv.getOfficialEmail(),
                pv.getOfficialPhone(),
                pv.getPhysicalAddress(),
                pv.getWebsite(),
                pv.getLinkedinUrl(),
                pv.getAuthorizedRepName(),
                pv.getAuthorizedRepRole(),
                pv.getStatus(),
                pv.getSubmittedAt(),
                pv.getVerifiedAt(),
                pv.getExpiresAt(),
                pv.getAdminNotes(),
                pv.getCreatedAt(),
                pv.getUpdatedAt()
        );
    }

    public ProviderVerificationEventResponse toEventResponse(ProviderVerificationEvent ev) {
        return new ProviderVerificationEventResponse(
                ev.getId(),
                ev.getEventType(),
                ev.getFromStatus(),
                ev.getToStatus(),
                ev.getNotes(),
                ev.getActor() != null ? ev.getActor().getId() : null,
                ev.getCreatedAt()
        );
    }
}