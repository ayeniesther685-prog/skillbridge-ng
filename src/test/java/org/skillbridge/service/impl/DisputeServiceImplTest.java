package org.skillbridge.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skillbridge.dto.request.DisputeMessageCreateRequest;
import org.skillbridge.dto.request.DisputeResolutionRequest;
import org.skillbridge.dto.request.OpenDisputeRequest;
import org.skillbridge.entity.Contract;
import org.skillbridge.entity.Dispute;
import org.skillbridge.entity.Milestone;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ContractStatus;
import org.skillbridge.enums.DisputeStatus;
import org.skillbridge.mapper.DisputeMapper;
import org.skillbridge.repository.ContractRepository;
import org.skillbridge.repository.DisputeEvidenceRepository;
import org.skillbridge.repository.DisputeMessageRepository;
import org.skillbridge.repository.DisputeRepository;
import org.skillbridge.repository.MilestoneRepository;
import org.skillbridge.security.CurrentUser;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DisputeServiceImplTest {

    private DisputeRepository disputeRepo;
    private DisputeMessageRepository messageRepo;
    private DisputeEvidenceRepository evidenceRepo;
    private ContractRepository contractRepo;
    private MilestoneRepository milestoneRepo;
    private CurrentUser currentUser;
    private DisputeServiceImpl service;

    private User client;
    private User freelancer;
    private User stranger;
    private Contract contract;
    private Milestone milestone;

    @BeforeEach
    void setUp() {
        disputeRepo = mock(DisputeRepository.class);
        messageRepo = mock(DisputeMessageRepository.class);
        evidenceRepo = mock(DisputeEvidenceRepository.class);
        contractRepo = mock(ContractRepository.class);
        milestoneRepo = mock(MilestoneRepository.class);
        currentUser = mock(CurrentUser.class);
        service = new DisputeServiceImpl(
                disputeRepo, messageRepo, evidenceRepo, contractRepo, milestoneRepo,
                new DisputeMapper(), currentUser);

        client = user(1L, "Client");
        freelancer = user(2L, "Freelancer");
        stranger = user(99L, "Stranger");

        contract = new Contract();
        contract.setId(10L);
        contract.setClient(client);
        contract.setFreelancer(freelancer);
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setTitle("Test Contract");

        milestone = new Milestone();
        milestone.setId(100L);
        milestone.setContract(contract);
    }

    private User user(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setFirstName(name);
        u.setLastName("");
        u.setEmail(name.toLowerCase() + "@example.com");
        return u;
    }

    // -------------- openDispute --------------

    @Test
    void openDispute_happyPath_setsDisputedAt() {
        when(currentUser.require()).thenReturn(client);
        when(contractRepo.findById(10L)).thenReturn(Optional.of(contract));
        when(disputeRepo.findOpenForContract(10L)).thenReturn(Optional.empty());
        when(disputeRepo.save(any())).thenAnswer(inv -> { Dispute d = inv.getArgument(0); d.setId(500L); return d; });
        when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepo.findByDisputeIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(evidenceRepo.findByDisputeIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

        var resp = service.openDispute(10L,
                new OpenDisputeRequest(null, "Reason that is definitely long enough to pass validation"));

        assertThat(resp.status()).isEqualTo(DisputeStatus.OPEN);
        assertThat(contract.getDisputedAt()).isNotNull();
    }

    @Test
    void openDispute_stranger_throwsAccessDenied() {
        when(currentUser.require()).thenReturn(stranger);
        when(contractRepo.findById(10L)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service.openDispute(10L,
                new OpenDisputeRequest(null, "Reason that is definitely long enough")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void openDispute_completedContract_throwsIllegalState() {
        contract.setStatus(ContractStatus.COMPLETED);
        when(currentUser.require()).thenReturn(client);
        when(contractRepo.findById(10L)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service.openDispute(10L,
                new OpenDisputeRequest(null, "Reason that is definitely long enough")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    void openDispute_existingOpenDispute_throwsIllegalState() {
        when(currentUser.require()).thenReturn(client);
        when(contractRepo.findById(10L)).thenReturn(Optional.of(contract));
        when(disputeRepo.findOpenForContract(10L)).thenReturn(Optional.of(new Dispute()));

        assertThatThrownBy(() -> service.openDispute(10L,
                new OpenDisputeRequest(null, "Reason that is definitely long enough")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already OPEN");
    }

    @Test
    void openDispute_milestoneNotOnContract_throwsIllegalArgument() {
        Contract otherContract = new Contract();
        otherContract.setId(999L);
        Milestone otherMilestone = new Milestone();
        otherMilestone.setId(200L);
        otherMilestone.setContract(otherContract);

        when(currentUser.require()).thenReturn(client);
        when(contractRepo.findById(10L)).thenReturn(Optional.of(contract));
        when(milestoneRepo.findById(200L)).thenReturn(Optional.of(otherMilestone));

        assertThatThrownBy(() -> service.openDispute(10L,
                new OpenDisputeRequest(200L, "Reason that is definitely long enough")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");
    }

    // -------------- adminResolve --------------

    @Test
    void adminResolve_happyPath_clearsDisputedAtWhenNoBlockingRemain() {
        Dispute d = openDispute(500L, 10L);
        when(currentUser.require()).thenReturn(stranger); // admin (not a participant, fine)
        when(disputeRepo.findById(500L)).thenReturn(Optional.of(d));
        when(disputeRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(disputeRepo.existsBlockingForContract(10L)).thenReturn(false);
        when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepo.findByDisputeIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(evidenceRepo.findByDisputeIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

        contract.setDisputedAt(java.time.Instant.now());

        service.adminResolve(500L,
                new DisputeResolutionRequest("RESOLVED_FOR_CLIENT", "Detailed resolution reason here"));

        assertThat(d.getStatus()).isEqualTo(DisputeStatus.RESOLVED_FOR_CLIENT);
        assertThat(d.getResolvedAt()).isNotNull();
        assertThat(contract.getDisputedAt()).isNull();
    }

    @Test
    void adminResolve_keepsDisputedAtWhenAnotherBlockingRemains() {
        Dispute d = openDispute(500L, 10L);
        when(currentUser.require()).thenReturn(stranger);
        when(disputeRepo.findById(500L)).thenReturn(Optional.of(d));
        when(disputeRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(disputeRepo.existsBlockingForContract(10L)).thenReturn(true); // another blocking dispute exists
        when(messageRepo.findByDisputeIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(evidenceRepo.findByDisputeIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

        contract.setDisputedAt(java.time.Instant.now());

        service.adminResolve(500L,
                new DisputeResolutionRequest("RESOLVED_FOR_FREELANCER", "Detailed resolution reason here"));

        assertThat(contract.getDisputedAt()).isNotNull();
    }

    @Test
    void adminResolve_invalidDecision_throwsIllegalArgument() {
        Dispute d = openDispute(500L, 10L);
        when(currentUser.require()).thenReturn(stranger);
        when(disputeRepo.findById(500L)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> service.adminResolve(500L,
                new DisputeResolutionRequest("BOGUS", "Detailed resolution reason here")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid decision");
    }

    @Test
    void adminResolve_alreadyTerminal_throwsIllegalState() {
        Dispute d = openDispute(500L, 10L);
        d.setStatus(DisputeStatus.RESOLVED_FOR_CLIENT);
        when(currentUser.require()).thenReturn(stranger);
        when(disputeRepo.findById(500L)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> service.adminResolve(500L,
                new DisputeResolutionRequest("RESOLVED_FOR_FREELANCER", "Detailed resolution")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminal");
    }

    // -------------- messages --------------

    @Test
    void addMessage_participant_canPost() {
        Dispute d = openDispute(500L, 10L);
        when(currentUser.require()).thenReturn(freelancer);
        when(disputeRepo.findById(500L)).thenReturn(Optional.of(d));
        when(messageRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepo.findByDisputeIdOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(evidenceRepo.findByDisputeIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

        var resp = service.addMessage(500L, new DisputeMessageCreateRequest("Hello admin"));
        assertThat(resp).isNotNull();
        verify(messageRepo).save(any());
    }

    @Test
    void addMessage_stranger_throwsAccessDenied() {
        Dispute d = openDispute(500L, 10L);
        when(currentUser.require()).thenReturn(stranger);
        when(disputeRepo.findById(500L)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> service.addMessage(500L, new DisputeMessageCreateRequest("hi")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    // -------------- helpers --------------

    private Dispute openDispute(Long id, Long contractId) {
        Dispute d = new Dispute();
        d.setId(id);
        d.setContract(contract);
        d.setOpenedBy(client);
        d.setReason("Detailed reason that is long enough");
        d.setStatus(DisputeStatus.OPEN);
        return d;
    }
}
