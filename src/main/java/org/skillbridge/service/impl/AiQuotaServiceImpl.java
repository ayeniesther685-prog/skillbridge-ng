package org.skillbridge.service.impl;

import org.skillbridge.dto.response.AiQuotaStatusResponse;
import org.skillbridge.dto.response.AiQuotaTierResponse;
import org.skillbridge.dto.response.AiUsageEventResponse;
import org.skillbridge.entity.AiQuotaTier;
import org.skillbridge.entity.AiUsageEvent;
import org.skillbridge.entity.User;
import org.skillbridge.entity.UserAiQuota;
import org.skillbridge.enums.AiFeature;
import org.skillbridge.enums.AiUsageStatus;
import org.skillbridge.exception.QuotaExceededException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.AiQuotaMapper;
import org.skillbridge.repository.AiQuotaTierRepository;
import org.skillbridge.repository.AiUsageEventRepository;
import org.skillbridge.repository.UserAiQuotaRepository;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AiQuotaServiceImpl {

    private static final ZoneId LAGOS = ZoneId.of("Africa/Lagos");
    private static final String DEFAULT_TIER = "FREE";

    private final UserAiQuotaRepository quotaRepository;
    private final AiQuotaTierRepository tierRepository;
    private final AiUsageEventRepository eventRepository;
    private final UserRepository userRepository;
    private final AiQuotaMapper mapper;
    private final CurrentUser currentUser;

    public AiQuotaServiceImpl(UserAiQuotaRepository quotaRepository,
                              AiQuotaTierRepository tierRepository,
                              AiUsageEventRepository eventRepository,
                              UserRepository userRepository,
                              AiQuotaMapper mapper,
                              CurrentUser currentUser) {
        this.quotaRepository = quotaRepository;
        this.tierRepository = tierRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    /**
     * THE COST-PROTECTION METHOD.
     *
     * Every future AI endpoint must call this BEFORE invoking the AI provider.
     * It atomically:
     *   1. Loads or creates the user's quota record (defaults to FREE tier)
     *   2. Lazily resets daily/monthly counters if a new period has started
     *   3. Checks whether the user has quota available
     *   4. Consumes 1 unit (from bonus_credits first, then from tier limit)
     *   5. Logs the attempt to ai_usage_events
     * If quota is exhausted, throws QuotaExceededException (-> HTTP 429).
     */
    @Transactional
    public void checkAndConsume(Long userId, AiFeature feature) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserAiQuota quota = loadOrCreateForUser(user);

        applyLazyResets(quota);

        int remaining = computeRemainingToday(quota);
        if (remaining <= 0) {
            logEvent(user, feature, AiUsageStatus.QUOTA_EXCEEDED,
                    "Daily limit reached. Resets at " + quota.getDailyResetAt());
            throw new QuotaExceededException(
                    "You have reached your daily AI limit for the " + quota.getTier().getTierName() + " tier.",
                    "Daily quota resets at",
                    quota.getDailyResetAt()
            );
        }

        int remainingMonth = computeRemainingThisMonth(quota);
        if (remainingMonth <= 0) {
            logEvent(user, feature, AiUsageStatus.QUOTA_EXCEEDED,
                    "Monthly limit reached. Resets at " + quota.getMonthlyResetAt());
            throw new QuotaExceededException(
                    "You have reached your monthly AI limit for the " + quota.getTier().getTierName() + " tier.",
                    "Monthly quota resets at",
                    quota.getMonthlyResetAt()
            );
        }

        // Consume: bonus first, then tier quota
        if (quota.getBonusCredits() > 0) {
            quota.setBonusCredits(quota.getBonusCredits() - 1);
        } else {
            quota.setUsedToday(quota.getUsedToday() + 1);
            quota.setUsedThisMonth(quota.getUsedThisMonth() + 1);
        }
        quotaRepository.save(quota);

        logEvent(user, feature, AiUsageStatus.SUCCESS, null);
    }

    /** Record a token usage count after the AI provider returns. */
    @Transactional
    public void recordTokens(Long userId, AiFeature feature, int tokensUsed) {
        // For now, this is a no-op beyond logging. In Slice 8, we'll update the
        // most recent SUCCESS event for this user+feature with the token count.
        // Kept as a hook so future code can extend without changing callers.
        List<AiUsageEvent> recent = eventRepository.findByUserIdAndCreatedAtAfter(userId, Instant.now().minusSeconds(300));
        recent.stream()
                .filter(e -> e.getFeature() == feature && e.getStatus() == AiUsageStatus.SUCCESS)
                .findFirst()
                .ifPresent(e -> {
                    e.setTokensUsed(tokensUsed);
                    eventRepository.save(e);
                });
    }

    /** Read current quota state. Does NOT consume. */
    @Transactional
     public AiQuotaStatusResponse getMyStatus() {
        User me = currentUser.require();
        UserAiQuota quota = loadOrCreateForUser(me);
        applyLazyResets(quota);
        quotaRepository.save(quota);

        int remainingToday = computeRemainingToday(quota);
        int remainingMonth = computeRemainingThisMonth(quota);
        return mapper.toStatus(quota, remainingToday, remainingMonth);
    }

    /** Read my usage history, paginated. */
    public Page<AiUsageEventResponse> listMyUsage(Pageable pageable) {
        User me = currentUser.require();
        return eventRepository.findByUserIdOrderByCreatedAtDesc(me.getId(), pageable)
                .map(mapper::toEventResponse);
    }

    /** Admin: grant bonus credits to a user. */
    @Transactional
    public AiQuotaStatusResponse grantCredits(Long targetUserId, int amount) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserAiQuota quota = loadOrCreateForUser(target);
        quota.setBonusCredits(quota.getBonusCredits() + amount);
        quotaRepository.save(quota);

        int remainingToday = computeRemainingToday(quota);
        int remainingMonth = computeRemainingThisMonth(quota);
        return mapper.toStatus(quota, remainingToday, remainingMonth);
    }

    /** Admin: change a user's tier. */
    @Transactional
    public AiQuotaStatusResponse changeTier(Long targetUserId, String tierName) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AiQuotaTier tier = tierRepository.findByTierNameIgnoreCase(tierName)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + tierName));
        if (!tier.isActive()) {
            throw new IllegalArgumentException("Tier is not active: " + tierName);
        }

        UserAiQuota quota = loadOrCreateForUser(target);
        quota.setTier(tier);
        quotaRepository.save(quota);

        int remainingToday = computeRemainingToday(quota);
        int remainingMonth = computeRemainingThisMonth(quota);
        return mapper.toStatus(quota, remainingToday, remainingMonth);
    }

    /** Admin: list all tiers. */
    public List<AiQuotaTierResponse> listTiers() {
        return tierRepository.findByActiveTrueOrderByDailyLimitAsc()
                .stream().map(mapper::toTierResponse).toList();
    }

    // ===== Internal helpers =====

    private UserAiQuota loadOrCreateForUser(User user) {
        return quotaRepository.findByUserId(user.getId()).orElseGet(() -> {
            AiQuotaTier freeTier = tierRepository.findByTierNameIgnoreCase(DEFAULT_TIER)
                    .orElseThrow(() -> new IllegalStateException(
                            "Default tier '" + DEFAULT_TIER + "' missing. Check V20 migration."));
            UserAiQuota q = new UserAiQuota();
            q.setUser(user);
            q.setTier(freeTier);
            q.setBonusCredits(0);
            q.setUsedToday(0);
            q.setUsedThisMonth(0);
            q.setDailyResetAt(nextMidnightLagos());
            q.setMonthlyResetAt(nextMonthStartLagos());
            return quotaRepository.save(q);
        });
    }

    private void applyLazyResets(UserAiQuota q) {
        Instant now = Instant.now();
        if (now.isAfter(q.getDailyResetAt())) {
            q.setUsedToday(0);
            q.setDailyResetAt(nextMidnightLagos());
        }
        if (now.isAfter(q.getMonthlyResetAt())) {
            q.setUsedThisMonth(0);
            q.setMonthlyResetAt(nextMonthStartLagos());
        }
    }

    private int computeRemainingToday(UserAiQuota q) {
        return q.getTier().getDailyLimit() - q.getUsedToday() + q.getBonusCredits();
    }

    private int computeRemainingThisMonth(UserAiQuota q) {
        return q.getTier().getMonthlyLimit() - q.getUsedThisMonth() + q.getBonusCredits();
    }

    private Instant nextMidnightLagos() {
        ZonedDateTime now = ZonedDateTime.now(LAGOS);
        return now.toLocalDate().plusDays(1).atStartOfDay(LAGOS).toInstant();
    }

    private Instant nextMonthStartLagos() {
        LocalDate today = LocalDate.now(LAGOS);
        return today.withDayOfMonth(1).plusMonths(1).atStartOfDay(LAGOS).toInstant();
    }

    private void logEvent(User user, AiFeature feature, AiUsageStatus status, String errorMessage) {
        AiUsageEvent e = new AiUsageEvent();
        e.setUser(user);
        e.setFeature(feature);
        e.setCost(1);
        e.setStatus(status);
        e.setErrorMessage(errorMessage);
        eventRepository.save(e);
    }
}