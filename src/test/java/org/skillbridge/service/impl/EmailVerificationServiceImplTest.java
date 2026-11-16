package org.skillbridge.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skillbridge.config.EmailProperties;
import org.skillbridge.email.EmailRenderService;
import org.skillbridge.email.EmailSender;
import org.skillbridge.email.VerificationTokenHasher;
import org.skillbridge.entity.User;
import org.skillbridge.entity.VerificationToken;
import org.skillbridge.enums.VerificationChannel;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.repository.VerificationTokenRepository;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmailVerificationServiceImplTest {

    private UserRepository userRepo;
    private VerificationTokenRepository tokenRepo;
    private EmailSender emailSender;
    private EmailVerificationServiceImpl service;
    private VerificationTokenHasher realHasher;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        tokenRepo = mock(VerificationTokenRepository.class);
        emailSender = mock(EmailSender.class);
        realHasher = new VerificationTokenHasher();
        EmailProperties props = new EmailProperties();
        props.setAppBaseUrl("http://test.local");
        props.setVerificationTtlHours(24);
        props.setFrom("noreply@test.local");
        // Real render service works on classpath templates — safe in unit test
        EmailRenderService render = new EmailRenderService();

        service = new EmailVerificationServiceImpl(
                userRepo, tokenRepo, realHasher, render, emailSender, props);
    }

    private User user(Long id, boolean verified) {
        User u = new User();
        u.setId(id);
        u.setFirstName("Test");
        u.setLastName("User");
        u.setEmail("u" + id + "@example.com");
        u.setEmailVerified(verified);
        return u;
    }

    @Test
    void issueVerification_skipsWhenAlreadyVerified() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L, true)));
        boolean sent = service.issueVerification(1L);
        assertThat(sent).isFalse();
        verify(tokenRepo, never()).save(any());
        verify(emailSender, never()).send(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void issueVerification_createsTokenAndSendsEmail() {
        when(userRepo.findById(2L)).thenReturn(Optional.of(user(2L, false)));
        when(tokenRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(emailSender.send(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        boolean sent = service.issueVerification(2L);

        assertThat(sent).isTrue();
        ArgumentCaptor<VerificationToken> cap = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepo).save(cap.capture());
        VerificationToken tok = cap.getValue();
        assertThat(tok.getChannel()).isEqualTo(VerificationChannel.EMAIL);
        assertThat(tok.getTokenHash()).hasSize(64); // SHA-256 hex
        assertThat(tok.getExpiresAt()).isAfter(Instant.now());
        assertThat(tok.getConsumedAt()).isNull();

        ArgumentCaptor<String> urlCap = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq("u2@example.com"), anyString(), anyString(), urlCap.capture());
        // Actually capture the HTML body; just confirm send was called with 4 args
        verify(emailSender).send(eq("u2@example.com"), anyString(), anyString(), anyString());
    }

    @Test
    void verify_happyPath_marksTokenConsumedAndUserVerified() {
        User u = user(3L, false);
        String raw = "rawtoken";
        String hash = realHasher.hash(raw);
        VerificationToken tok = new VerificationToken();
        tok.setId(100L);
        tok.setUser(u);
        tok.setTokenHash(hash);
        tok.setChannel(VerificationChannel.EMAIL);
        tok.setExpiresAt(Instant.now().plusSeconds(3600));

        when(tokenRepo.findByTokenHash(hash)).thenReturn(Optional.of(tok));
        when(tokenRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.verify(raw);

        assertThat(tok.getConsumedAt()).isNotNull();
        assertThat(u.isEmailVerified()).isTrue();
        assertThat(u.getEmailVerifiedAt()).isNotNull();
        verify(userRepo).save(u);
    }

    @Test
    void verify_unknownToken_throws404() {
        when(tokenRepo.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.verify("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid verification link");
    }

    @Test
    void verify_consumedToken_throwsIllegalState() {
        VerificationToken tok = new VerificationToken();
        tok.setUser(user(4L, false));
        tok.setExpiresAt(Instant.now().plusSeconds(3600));
        tok.setConsumedAt(Instant.now());
        when(tokenRepo.findByTokenHash(anyString())).thenReturn(Optional.of(tok));

        assertThatThrownBy(() -> service.verify("x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void verify_expiredToken_throwsIllegalState() {
        VerificationToken tok = new VerificationToken();
        tok.setUser(user(5L, false));
        tok.setExpiresAt(Instant.now().minusSeconds(60)); // expired
        when(tokenRepo.findByTokenHash(anyString())).thenReturn(Optional.of(tok));

        assertThatThrownBy(() -> service.verify("x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void resendForEmail_silentlyNoopsForNonexistentEmail() {
        when(userRepo.findByEmailIgnoreCase("ghost@example.com")).thenReturn(Optional.empty());
        assertThatCode(() -> service.resendForEmail("ghost@example.com")).doesNotThrowAnyException();
        verify(tokenRepo, never()).save(any());
    }

    @Test
    void resendForEmail_skipsVerifiedUsers() {
        when(userRepo.findByEmailIgnoreCase("verified@example.com"))
                .thenReturn(Optional.of(user(6L, true)));
        service.resendForEmail("verified@example.com");
        verify(tokenRepo, never()).save(any());
    }

    @Test
    void resendForEmail_issuesNewTokenForUnverifiedUser() {
        User u = user(7L, false);
        when(userRepo.findByEmailIgnoreCase("u7@example.com")).thenReturn(Optional.of(u));
        when(userRepo.findById(7L)).thenReturn(Optional.of(u));
        when(tokenRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(emailSender.send(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        service.resendForEmail("u7@example.com");

        verify(tokenRepo).save(any());
        verify(emailSender).send(eq("u7@example.com"), anyString(), anyString(), anyString());
    }
}
