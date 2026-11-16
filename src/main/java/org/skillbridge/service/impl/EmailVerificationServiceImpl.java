package org.skillbridge.service.impl;

import org.skillbridge.config.EmailProperties;
import org.skillbridge.email.EmailRenderService;
import org.skillbridge.email.EmailSender;
import org.skillbridge.email.VerificationEmailPayload;
import org.skillbridge.email.VerificationTokenHasher;
import org.skillbridge.entity.User;
import org.skillbridge.entity.VerificationToken;
import org.skillbridge.enums.VerificationChannel;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.repository.VerificationTokenRepository;
import org.skillbridge.service.EmailVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final VerificationTokenHasher hasher;
    private final EmailRenderService renderService;
    private final EmailSender emailSender;
    private final EmailProperties emailProperties;

    public EmailVerificationServiceImpl(UserRepository userRepository,
                                        VerificationTokenRepository tokenRepository,
                                        VerificationTokenHasher hasher,
                                        EmailRenderService renderService,
                                        EmailSender emailSender,
                                        EmailProperties emailProperties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.hasher = hasher;
        this.renderService = renderService;
        this.emailSender = emailSender;
        this.emailProperties = emailProperties;
    }

    @Override
    public boolean issueVerification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.isEmailVerified()) {
            log.debug("User {} already verified — skipping email issue", userId);
            return false;
        }

        // Invalidate any outstanding email tokens for this user.
        Instant now = Instant.now();
        tokenRepository.invalidateAllForUser(user.getId(), VerificationChannel.EMAIL, now);

        // Create new token.
        String rawToken = hasher.generate();
        Instant expiresAt = now.plus(emailProperties.getVerificationTtlHours(), ChronoUnit.HOURS);

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setTokenHash(hasher.hash(rawToken));
        token.setChannel(VerificationChannel.EMAIL);
        token.setExpiresAt(expiresAt);
        tokenRepository.save(token);

        // Build and send email.
        String baseUrl = emailProperties.getAppBaseUrl();
        String verifyUrl = trimTrailingSlash(baseUrl) + "/verify-email?token=" + rawToken;

        VerificationEmailPayload payload = new VerificationEmailPayload(
                user.getFirstName(),
                user.getEmail(),
                verifyUrl,
                expiresAt,
                emailProperties.getVerificationTtlHours()
        );

        String html = renderService.renderVerifyEmailHtml(payload);
        String text = renderService.renderVerifyEmailText(payload);

        boolean sent = emailSender.send(
                user.getEmail(),
                "Verify your SkillBridge NG email",
                html,
                text
        );

        if (!sent) {
            log.warn("Verification email could not be delivered to {} — token still valid", user.getEmail());
        }

        return true;
    }

    @Override
    public void verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Verification token is required");
        }

        String hash = hasher.hash(rawToken);
        VerificationToken token = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification link"));

        if (token.isConsumed()) {
            throw new IllegalStateException("This verification link has already been used");
        }
        if (token.isExpired()) {
            throw new IllegalStateException("This verification link has expired");
        }

        // Mark token consumed.
        token.setConsumedAt(Instant.now());
        tokenRepository.save(token);

        // Mark user verified (idempotent).
        User user = token.getUser();
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(Instant.now());
            userRepository.save(user);
            log.info("Email verified for user id={} email={}", user.getId(), user.getEmail());
        }
    }

    @Override
    public void resendForEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        // Silent no-op if user doesn't exist — prevents account enumeration.
        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                issueVerification(user.getId());
            } else {
                log.debug("Resend requested for already-verified user — ignored");
            }
        });
    }

    @Override
    @Transactional
    public int purgeExpired() {
        int n = tokenRepository.deleteAllExpiredBefore(Instant.now());
        if (n > 0) log.info("Purged {} expired verification tokens", n);
        return n;
    }

    private static String trimTrailingSlash(String s) {
        return s != null && s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
