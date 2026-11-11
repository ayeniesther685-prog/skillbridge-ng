package org.skillbridge.service.impl;

import org.skillbridge.ai.GeminiClient;
import org.skillbridge.dto.request.AiTutorRequest;
import org.skillbridge.dto.response.AiQuotaStatusResponse;
import org.skillbridge.dto.response.AiTutorResponse;
import org.skillbridge.entity.User;
import org.skillbridge.enums.AiFeature;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AiTutorServiceImpl {

    private static final String SYSTEM_CONTEXT =
            "You are SkillBridge NG's AI Tutor, helping Nigerian students learn " +
            "programming, design, and business skills. Answer clearly, concisely, " +
            "and with practical examples. Do not make up facts. If unsure, say so.";

    private final GeminiClient geminiClient;
    private final AiQuotaServiceImpl quotaService;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public AiTutorServiceImpl(GeminiClient geminiClient,
                              AiQuotaServiceImpl quotaService,
                              UserRepository userRepository,
                              CurrentUser currentUser) {
        this.geminiClient = geminiClient;
        this.quotaService = quotaService;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public AiTutorResponse ask(AiTutorRequest req) {
        User me = currentUser.require();

        // 1. Consume one quota unit. Throws QuotaExceededException (429) if exhausted.
        quotaService.checkAndConsume(me.getId(), AiFeature.INSTRUCTOR_CHAT);

        // 2. Call the AI. If the AI fails, we still log an ERROR event.
        String answer;
        try {
            answer = geminiClient.generate(req.message(), SYSTEM_CONTEXT);
        } catch (Exception aiEx) {
            quotaService.recordTokens(me.getId(), AiFeature.INSTRUCTOR_CHAT, 0);
            throw new IllegalStateException(
                    "AI Tutor is temporarily unavailable. Please try again in a minute.", aiEx);
        }

        // 3. Estimate tokens used and record
        int estimatedTokens = geminiClient.estimateTokens(answer);
        quotaService.recordTokens(me.getId(), AiFeature.INSTRUCTOR_CHAT, estimatedTokens);

        // 4. Get updated quota state to return remaining to caller
        AiQuotaStatusResponse status = quotaService.getMyStatus();

        return new AiTutorResponse(answer, estimatedTokens, status.remainingToday());
    }
}