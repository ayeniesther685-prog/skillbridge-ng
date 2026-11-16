package org.skillbridge.mapper;

import org.skillbridge.dto.response.DisputeEvidenceResponse;
import org.skillbridge.dto.response.DisputeMessageResponse;
import org.skillbridge.dto.response.DisputeResponse;
import org.skillbridge.dto.response.DisputeSummaryResponse;
import org.skillbridge.entity.Dispute;
import org.skillbridge.entity.DisputeEvidence;
import org.skillbridge.entity.DisputeMessage;
import org.skillbridge.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DisputeMapper {

    public DisputeSummaryResponse toSummary(Dispute d) {
        return new DisputeSummaryResponse(
                d.getId(),
                d.getContract().getId(),
                d.getMilestone() != null ? d.getMilestone().getId() : null,
                d.getOpenedBy().getId(),
                nameOf(d.getOpenedBy()),
                d.getReason(),
                d.getStatus(),
                d.getCreatedAt(),
                d.getResolvedAt()
        );
    }

    public DisputeResponse toFull(Dispute d,
                                  List<DisputeMessage> messages,
                                  List<DisputeEvidence> evidence) {
        return new DisputeResponse(
                d.getId(),
                d.getContract().getId(),
                d.getMilestone() != null ? d.getMilestone().getId() : null,
                d.getContract().getTitle(),
                d.getOpenedBy().getId(),
                nameOf(d.getOpenedBy()),
                d.getReason(),
                d.getStatus(),
                d.getResolution(),
                d.getResolvedBy() != null ? d.getResolvedBy().getId() : null,
                d.getResolvedBy() != null ? nameOf(d.getResolvedBy()) : null,
                d.getResolvedAt(),
                d.getCreatedAt(),
                d.getUpdatedAt(),
                messages.stream().map(this::toMessageResponse).toList(),
                evidence.stream().map(this::toEvidenceResponse).toList()
        );
    }

    public DisputeMessageResponse toMessageResponse(DisputeMessage m) {
        return new DisputeMessageResponse(
                m.getId(),
                m.getAuthor().getId(),
                nameOf(m.getAuthor()),
                m.getMessage(),
                m.isFromAdmin(),
                m.getCreatedAt()
        );
    }

    public DisputeEvidenceResponse toEvidenceResponse(DisputeEvidence e) {
        return new DisputeEvidenceResponse(
                e.getId(),
                e.getUploadedBy().getId(),
                nameOf(e.getUploadedBy()),
                e.getEvidenceType(),
                e.getUrl(),
                e.getDescription(),
                e.getCreatedAt()
        );
    }

    private static String nameOf(User u) {
        if (u == null) return null;
        return (u.getFirstName() + " " + u.getLastName()).trim();
    }
}
