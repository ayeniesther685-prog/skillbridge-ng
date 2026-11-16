package org.skillbridge.service.impl;

import org.skillbridge.dto.request.MilestoneCreateRequest;
import org.skillbridge.dto.request.MilestoneUpdateRequest;
import org.skillbridge.dto.response.MilestoneResponse;
import org.skillbridge.entity.Contract;
import org.skillbridge.entity.Milestone;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ContractStatus;
import org.skillbridge.enums.MilestoneStatus;
import org.skillbridge.enums.NotificationType;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.MilestoneMapper;
import org.skillbridge.repository.ContractRepository;
import org.skillbridge.repository.MilestoneRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MilestoneServiceImpl {

    private final MilestoneRepository milestoneRepository;
    private final ContractRepository contractRepository;
    private final MilestoneMapper milestoneMapper;
    private final CurrentUser currentUser;
    private final NotificationServiceImpl notificationService;
    private final CommissionServiceImpl commissionService;

    public MilestoneServiceImpl(MilestoneRepository milestoneRepository,
                                ContractRepository contractRepository,
                                MilestoneMapper milestoneMapper,
                                CurrentUser currentUser,
                                NotificationServiceImpl notificationService,
                                CommissionServiceImpl commissionService) {
        this.milestoneRepository = milestoneRepository;
        this.contractRepository = contractRepository;
        this.milestoneMapper = milestoneMapper;
        this.currentUser = currentUser;
        this.notificationService = notificationService;
        this.commissionService = commissionService;
    }

    public List<MilestoneResponse> listForContract(Long contractId) {
        User me = currentUser.require();
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        assertParticipant(c, me);
        return milestoneRepository.findByContractIdOrderByCreatedAtAsc(contractId)
                .stream().map(milestoneMapper::toResponse).toList();
    }

    @Transactional
    public MilestoneResponse create(Long contractId, MilestoneCreateRequest req) {
        User me = currentUser.require();
        Contract c = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (!c.getClient().getId().equals(me.getId())) {
            throw new AccessDeniedException("Only the client can add milestones");
        }
        if (c.getStatus() == ContractStatus.COMPLETED || c.getStatus() == ContractStatus.CANCELLED) {
            throw new IllegalStateException("Contract is terminal");
        }

        Milestone m = new Milestone();
        m.setContract(c);
        m.setTitle(req.title());
        m.setDescription(req.description());
        m.setAmount(req.amount());
        m.setDueDate(req.dueDate());
        m.setStatus(MilestoneStatus.PENDING);
        return milestoneMapper.toResponse(milestoneRepository.save(m));
    }

    @Transactional
    public MilestoneResponse update(Long id, MilestoneUpdateRequest req) {
        User me = currentUser.require();
        Milestone m = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
        if (!m.getContract().getClient().getId().equals(me.getId())) {
            throw new AccessDeniedException("Only the client can edit milestones");
        }
        if (m.getStatus() != MilestoneStatus.PENDING) {
            throw new IllegalStateException("Only PENDING milestones can be edited");
        }
        m.setTitle(req.title());
        m.setDescription(req.description());
        m.setAmount(req.amount());
        m.setDueDate(req.dueDate());
        return milestoneMapper.toResponse(milestoneRepository.save(m));
    }

    @Transactional
    public MilestoneResponse changeStatus(Long id, MilestoneStatus newStatus) {
        User me = currentUser.require();
        Milestone m = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
        Contract c = m.getContract();
        boolean isClient = c.getClient().getId().equals(me.getId());
        boolean isFreelancer = c.getFreelancer().getId().equals(me.getId());
        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException("Not your milestone");
        }

        // IN_PROGRESS and COMPLETED are set by the freelancer;
        // APPROVED is set by the client.
        if (newStatus == MilestoneStatus.APPROVED && !isClient) {
            throw new AccessDeniedException("Only the client can approve milestones");
        }
        if ((newStatus == MilestoneStatus.IN_PROGRESS || newStatus == MilestoneStatus.COMPLETED)
                && !isFreelancer) {
            throw new AccessDeniedException("Only the freelancer can progress milestones");
        }

        MilestoneStatus current = m.getStatus();
        if (!current.allowedNext().contains(newStatus)) {
            throw new IllegalStateException("Invalid milestone transition: " + current + " -> " + newStatus);
        }

        m.setStatus(newStatus);
        if (newStatus == MilestoneStatus.COMPLETED) {
            m.setCompletedAt(Instant.now());
        }
        Milestone saved = milestoneRepository.save(m);

        notificationService.notify(
                isClient ? c.getFreelancer() : c.getClient(),
                NotificationType.CONTRACT,
                "Milestone " + newStatus,
                "Milestone '" + m.getTitle() + "' is now " + newStatus
        );

        // If all milestones are APPROVED, mark the contract COMPLETED.
        List<Milestone> all = milestoneRepository.findByContractIdOrderByCreatedAtAsc(c.getId());
        boolean allApproved = !all.isEmpty() &&
                all.stream().allMatch(x -> x.getStatus() == MilestoneStatus.APPROVED);
        if (allApproved && c.getStatus() != ContractStatus.COMPLETED) {
            c.setStatus(ContractStatus.COMPLETED);
            contractRepository.save(c);
            commissionService.recordCommissionForContract(c);
        }

        return milestoneMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        User me = currentUser.require();
        Milestone m = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
        if (!m.getContract().getClient().getId().equals(me.getId())) {
            throw new AccessDeniedException("Only the client can delete milestones");
        }
        if (m.getStatus() != MilestoneStatus.PENDING) {
            throw new IllegalStateException("Only PENDING milestones can be deleted");
        }
        milestoneRepository.delete(m);
    }

    private static void assertParticipant(Contract c, User me) {
        boolean isClient = c.getClient().getId().equals(me.getId());
        boolean isFreelancer = c.getFreelancer().getId().equals(me.getId());
        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException("Not your contract");
        }
    }
}