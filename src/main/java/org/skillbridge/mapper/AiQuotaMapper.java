package org.skillbridge.mapper;

import org.skillbridge.dto.response.AiQuotaStatusResponse;
import org.skillbridge.dto.response.AiQuotaTierResponse;
import org.skillbridge.dto.response.AiUsageEventResponse;
import org.skillbridge.entity.AiQuotaTier;
import org.skillbridge.entity.AiUsageEvent;
import org.skillbridge.entity.UserAiQuota;
import org.springframework.stereotype.Component;

@Component
public class AiQuotaMapper {

    public AiQuotaStatusResponse toStatus(UserAiQuota q, int remainingToday, int remainingThisMonth) {
        return new AiQuotaStatusResponse(
                q.getUser().getId(),
                q.getTier().getTierName(),
                q.getTier().getDailyLimit(),
                q.getTier().getMonthlyLimit(),
                q.getUsedToday(),
                q.getUsedThisMonth(),
                q.getBonusCredits(),
                remainingToday,
                remainingThisMonth,
                q.getDailyResetAt(),
                q.getMonthlyResetAt()
        );
    }

    public AiUsageEventResponse toEventResponse(AiUsageEvent e) {
        return new AiUsageEventResponse(
                e.getId(),
                e.getFeature(),
                e.getCost(),
                e.getStatus(),
                e.getTokensUsed(),
                e.getErrorMessage(),
                e.getCreatedAt()
        );
    }

    public AiQuotaTierResponse toTierResponse(AiQuotaTier t) {
        return new AiQuotaTierResponse(
                t.getId(),
                t.getTierName(),
                t.getDailyLimit(),
                t.getMonthlyLimit(),
                t.isActive()
        );
    }
}