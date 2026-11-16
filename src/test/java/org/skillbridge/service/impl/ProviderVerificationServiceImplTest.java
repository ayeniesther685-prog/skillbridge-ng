package org.skillbridge.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skillbridge.dto.request.ProviderVerificationSubmitRequest;
import org.skillbridge.entity.ProviderVerification;
import org.skillbridge.entity.ProviderVerificationEvent;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ProviderType;
import org.skillbridge.enums.VerificationEventType;
import org.skillbridge.enums.VerificationStatus;
import org.skillbridge.mapper.ProviderVerificationMapper;
import org.skillbridge.repository.ProviderVerificationEventRepository;
import org.skillbridge.repository.ProviderVerificationRepository;
import org.skillbridge.security.CurrentUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProviderVerificationServiceImplTest {

    private ProviderVerificationRepository repo;
    private ProviderVerificationEventRepository eventRepo;
    private CurrentUser currentUser;
    private ProviderVerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(ProviderVerificationRepository.class);
        eventRepo = mock(ProviderVerificationEventRepository.class);
        currentUser = mock(CurrentUser.class);
        service = new ProviderVerificationServiceImpl(
                repo, eventRepo, new ProviderVerificationMapper(), currentUser);
    }

    private User sampleUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setFirstName("Test");
        u.setLastName("Provider");
        u.setEmail("test" + id + "@example.com");
        return u;
    }

    private ProviderVerificationSubmitRequest validRequest() {
        return new ProviderVerificationSubmitRequest(
                ProviderType.EMPLOYER, "Acme Ltd", "RC123456",
                "hr@acme.com", "+2347000000000", "12 Lagos St",
                "https://acme.com", "https://linkedin.com/company/acme",
                "Ada Obi", "Director");
    }

    @Test
    void submit_movesToPending_andLogsEvent() {
        User me = sampleUser(1L);
        when(currentUser.require()).thenReturn(me);
        when(repo.findByUserId(1L)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> {
            ProviderVerification pv = inv.getArgument(0);
            pv.setId(100L);
            return pv;
        });

        var response = service.submit(validRequest());

        assertThat(response.status()).isEqualTo(VerificationStatus.PENDING);
        assertThat(response.submittedAt()).isNotNull();
        assertThat(response.legalName()).isEqualTo("Acme Ltd");

        ArgumentCaptor<ProviderVerificationEvent> evCap =
                ArgumentCaptor.forClass(ProviderVerificationEvent.class);
        verify(eventRepo).save(evCap.capture());
        assertThat(evCap.getValue().getEventType()).isEqualTo(VerificationEventType.SUBMITTED);
        assertThat(evCap.getValue().getToStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void submit_rejectsWhenAlreadyPending() {
        User me = sampleUser(1L);
        when(currentUser.require()).thenReturn(me);
        ProviderVerification existing = new ProviderVerification();
        existing.setUser(me);
        existing.setStatus(VerificationStatus.PENDING);
        when(repo.findByUserId(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.submit(validRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pending");
    }

    @Test
    void submit_rejectsWhenAlreadyVerified() {
        User me = sampleUser(1L);
        when(currentUser.require()).thenReturn(me);
        ProviderVerification existing = new ProviderVerification();
        existing.setUser(me);
        existing.setStatus(VerificationStatus.VERIFIED);
        when(repo.findByUserId(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.submit(validRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already verified");
    }

    @Test
    void adminApprove_onlyWorksOnPending() {
        User admin = sampleUser(99L);
        when(currentUser.require()).thenReturn(admin);

        ProviderVerification pv = new ProviderVerification();
        pv.setId(1L);
        pv.setUser(sampleUser(1L));
        pv.setStatus(VerificationStatus.UNVERIFIED);
        when(repo.findById(1L)).thenReturn(Optional.of(pv));

        assertThatThrownBy(() -> service.adminApprove(1L, "ok"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void adminApprove_setsVerifiedAndLogsEvent() {
        User admin = sampleUser(99L);
        when(currentUser.require()).thenReturn(admin);

        ProviderVerification pv = new ProviderVerification();
        pv.setId(1L);
        pv.setUser(sampleUser(1L));
        pv.setStatus(VerificationStatus.PENDING);
        when(repo.findById(1L)).thenReturn(Optional.of(pv));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.adminApprove(1L, "looks good");

        assertThat(result.status()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(result.verifiedAt()).isNotNull();
        assertThat(result.expiresAt()).isNotNull();
        verify(eventRepo).save(any(ProviderVerificationEvent.class));
    }

    @Test
    void adminReject_onlyWorksOnPending() {
        User admin = sampleUser(99L);
        when(currentUser.require()).thenReturn(admin);

        ProviderVerification pv = new ProviderVerification();
        pv.setId(1L);
        pv.setUser(sampleUser(1L));
        pv.setStatus(VerificationStatus.VERIFIED);
        when(repo.findById(1L)).thenReturn(Optional.of(pv));

        assertThatThrownBy(() -> service.adminReject(1L, "bad"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void isVerified_returnsTrueOnlyForVerifiedStatus() {
        ProviderVerification verified = new ProviderVerification();
        verified.setStatus(VerificationStatus.VERIFIED);
        when(repo.findByUserId(1L)).thenReturn(Optional.of(verified));
        assertThat(service.isVerified(1L)).isTrue();

        ProviderVerification pending = new ProviderVerification();
        pending.setStatus(VerificationStatus.PENDING);
        when(repo.findByUserId(2L)).thenReturn(Optional.of(pending));
        assertThat(service.isVerified(2L)).isFalse();

        when(repo.findByUserId(3L)).thenReturn(Optional.empty());
        assertThat(service.isVerified(3L)).isFalse();
    }

    @Test
    void getStatusForUser_returnsUnverifiedWhenNoRow() {
        when(repo.findByUserId(99L)).thenReturn(Optional.empty());
        var result = service.getStatusForUser(99L);
        assertThat(result.status()).isEqualTo(VerificationStatus.UNVERIFIED);
        assertThat(result.verified()).isFalse();
    }
}
