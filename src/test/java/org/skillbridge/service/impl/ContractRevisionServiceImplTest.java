package org.skillbridge.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skillbridge.dto.request.RevisionRequestCreateRequest;
import org.skillbridge.dto.request.RevisionResponseUpdateRequest;
import org.skillbridge.entity.Contract;
import org.skillbridge.entity.ContractRevision;
import org.skillbridge.entity.Milestone;
import org.skillbridge.entity.User;
import org.skillbridge.enums.MilestoneStatus;
import org.skillbridge.enums.RevisionStatus;
import org.skillbridge.exception.NewRequirementsException;
import org.skillbridge.exception.RevisionLimitExceededException;
import org.skillbridge.repository.ContractRevisionRepository;
import org.skillbridge.repository.MilestoneRepository;
import org.skillbridge.security.CurrentUser;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContractRevisionServiceImplTest {

    private ContractRevisionRepository revisionRepo;
    private MilestoneRepository milestoneRepo;
    private CurrentUser currentUser;
    private ContractRevisionServiceImpl service;

    private User client;
    private User freelancer;
    private Contract contract;
    private Milestone milestone;

    @BeforeEach
    void setUp() {
        revisionRepo = mock(ContractRevisionRepository.class);
        milestoneRepo = mock(MilestoneRepository.class);
        currentUser = mock(CurrentUser.class);
        service = new ContractRevisionServiceImpl(revisionRepo, milestoneRepo, currentUser);

        client = user(1L, "Client");
        freelancer = user(2L, "Freelancer");

        contract = new Contract();
        contract.setId(10L);
        contract.setClient(client);
        contract.setFreelancer(freelancer);

        milestone = new Milestone();
        milestone.setId(100L);
        milestone.setContract(contract);
        milestone.setStatus(MilestoneStatus.SUBMITTED);
        milestone.setRevisionLimit(3);
        milestone.setRevisionCount(0);
    }

    private User user(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setFirstName(name);
        u.setLastName("");
        u.setEmail(name.toLowerCase() + "@example.com");
        return u;
    }

    // --------------------- requestRevision ---------------------

    @Test
    void requestRevision_happyPath_incrementsCountAndSetsState() {
        when(currentUser.require()).thenReturn(client);
        when(milestoneRepo.findById(100L)).thenReturn(Optional.of(milestone));
        when(revisionRepo.findOpenByMilestone(100L)).thenReturn(List.of());
        when(revisionRepo.save(any())).thenAnswer(inv -> {
            ContractRevision r = inv.getArgument(0);
            r.setId(999L);
            return r;
        });
        when(milestoneRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.requestRevision(100L, new RevisionRequestCreateRequest("Please fix the API contract", false));

        assertThat(resp.status()).isEqualTo(RevisionStatus.OPEN);
        assertThat(resp.isNewRequirements()).isFalse();
        assertThat(milestone.getRevisionCount()).isEqualTo(1);
        assertThat(milestone.getStatus()).isEqualTo(MilestoneStatus.REVISION_REQUESTED);
    }

    @Test
    void requestRevision_wrongRole_throwsAccessDenied() {
        when(currentUser.require()).thenReturn(freelancer); // not the client
        when(milestoneRepo.findById(100L)).thenReturn(Optional.of(milestone));

        assertThatThrownBy(() -> service.requestRevision(100L,
                new RevisionRequestCreateRequest("Please fix", false)))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Only the contract's client");
    }

    @Test
    void requestRevision_notSubmitted_throwsIllegalState() {
        milestone.setStatus(MilestoneStatus.IN_PROGRESS);
        when(currentUser.require()).thenReturn(client);
        when(milestoneRepo.findById(100L)).thenReturn(Optional.of(milestone));

        assertThatThrownBy(() -> service.requestRevision(100L,
                new RevisionRequestCreateRequest("Please fix", false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SUBMITTED");
    }

    @Test
    void requestRevision_openRevisionExists_throwsIllegalState() {
        when(currentUser.require()).thenReturn(client);
        when(milestoneRepo.findById(100L)).thenReturn(Optional.of(milestone));
        when(revisionRepo.findOpenByMilestone(100L)).thenReturn(List.of(new ContractRevision()));

        assertThatThrownBy(() -> service.requestRevision(100L,
                new RevisionRequestCreateRequest("Please fix", false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already OPEN");
    }

    @Test
    void requestRevision_atLimit_throwsRevisionLimitExceeded() {
        milestone.setRevisionCount(3);
        when(currentUser.require()).thenReturn(client);
        when(milestoneRepo.findById(100L)).thenReturn(Optional.of(milestone));
        when(revisionRepo.findOpenByMilestone(100L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.requestRevision(100L,
                new RevisionRequestCreateRequest("Please fix", false)))
                .isInstanceOf(RevisionLimitExceededException.class)
                .hasMessageContaining("Revision limit reached");
    }

    @Test
    void requestRevision_newRequirements_throwsAndDoesNotConsumeSlot() {
        when(currentUser.require()).thenReturn(client);
        when(milestoneRepo.findById(100L)).thenReturn(Optional.of(milestone));
        when(revisionRepo.findOpenByMilestone(100L)).thenReturn(List.of());
        when(revisionRepo.save(any())).thenAnswer(inv -> {
            ContractRevision r = inv.getArgument(0);
            r.setId(555L);
            return r;
        });

        assertThatThrownBy(() -> service.requestRevision(100L,
                new RevisionRequestCreateRequest("Add a whole new feature", true)))
                .isInstanceOf(NewRequirementsException.class)
                .hasMessageContaining("new milestone");

        // Slot not consumed
        assertThat(milestone.getRevisionCount()).isEqualTo(0);
        assertThat(milestone.getStatus()).isEqualTo(MilestoneStatus.SUBMITTED);
    }

    // --------------------- respondToRevision ---------------------

    @Test
    void respondToRevision_accept_movesMilestoneBackToInProgress() {
        ContractRevision rev = openRevision(50L, milestone);
        when(currentUser.require()).thenReturn(freelancer);
        when(revisionRepo.findById(50L)).thenReturn(Optional.of(rev));
        when(revisionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(milestoneRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.respondToRevision(50L, new RevisionResponseUpdateRequest(true, "Will fix today"));

        assertThat(resp.status()).isEqualTo(RevisionStatus.ACCEPTED);
        assertThat(milestone.getStatus()).isEqualTo(MilestoneStatus.IN_PROGRESS);
        assertThat(rev.getResolvedAt()).isNotNull();
    }

    @Test
    void respondToRevision_reject_keepsMilestoneSubmitted() {
        ContractRevision rev = openRevision(51L, milestone);
        when(currentUser.require()).thenReturn(freelancer);
        when(revisionRepo.findById(51L)).thenReturn(Optional.of(rev));
        when(revisionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(milestoneRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.respondToRevision(51L, new RevisionResponseUpdateRequest(false, "Out of scope"));

        assertThat(resp.status()).isEqualTo(RevisionStatus.REJECTED);
        assertThat(milestone.getStatus()).isEqualTo(MilestoneStatus.SUBMITTED);
    }

    @Test
    void respondToRevision_wrongRole_throwsAccessDenied() {
        ContractRevision rev = openRevision(52L, milestone);
        when(currentUser.require()).thenReturn(client); // client, not freelancer
        when(revisionRepo.findById(52L)).thenReturn(Optional.of(rev));

        assertThatThrownBy(() -> service.respondToRevision(52L,
                new RevisionResponseUpdateRequest(true, "OK")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Only the contract's freelancer");
    }

    @Test
    void respondToRevision_notOpen_throwsIllegalState() {
        ContractRevision rev = openRevision(53L, milestone);
        rev.setStatus(RevisionStatus.ACCEPTED);
        when(currentUser.require()).thenReturn(freelancer);
        when(revisionRepo.findById(53L)).thenReturn(Optional.of(rev));

        assertThatThrownBy(() -> service.respondToRevision(53L,
                new RevisionResponseUpdateRequest(true, "OK")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not OPEN");
    }

    private ContractRevision openRevision(Long id, Milestone m) {
        ContractRevision r = new ContractRevision();
        r.setId(id);
        r.setMilestone(m);
        r.setRequestedBy(client);
        r.setReason("Please fix");
        r.setStatus(RevisionStatus.OPEN);
        r.setNewRequirements(false);
        return r;
    }
}
