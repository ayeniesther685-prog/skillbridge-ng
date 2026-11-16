package org.skillbridge.service.impl;

import org.skillbridge.dto.request.DisputeEvidenceCreateRequest;
import org.skillbridge.dto.request.DisputeMessageCreateRequest;
import org.skillbridge.dto.request.DisputeResolutionRequest;
import org.skillbridge.dto.request.OpenDisputeRequest;
import org.skillbridge.dto.response.DisputeResponse;
import org.skillbridge.dto.response.DisputeSummaryResponse;
import org.skillbridge.entity.Contract;
import org.skillbridge.entity.Dispute;
import org.skillbridge.entity.DisputeEvidence;
import org.skillbridge.entity.DisputeMessage;
import org.skillbridge.entity.Milestone;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ContractStatus;
import org.skillbridge.enums.DisputeEvidenceType;
import org.skillbridge.enums.DisputeStatus;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.DisputeMapper;
import org.skillbridge.repository.ContractRepository;
import org.skillbridge.repository.DisputeEvidenceRepository;
import org.skillbridge.repository.DisputeMessageRepository;
import org.skillbridge.repository.DisputeRepository;
import org.skillbridge.repository.MilestoneRepository;
import org.skillbridge.security.CurrentUser;
import org.skillbridge.service.DisputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DisputeServiceImpl implements DisputeService {

    private static final Logger log = LoggerFactory.getLogger(DisputeServiceImpl.class);

    private final DisputeRepository disputeRepository;
    private final DisputeMessageRepository messageRepository;
    private final DisputeEvidenceRepository evidenceRepository;
    private final ContractRepository contractRepository;
    private final MilestoneRepository milestoneRepository;
    private final DisputeMapper mapper;
    private final CurrentUser currentUser;

    public DisputeServiceImpl(DisputeRepository disputeRepository,
                              DisputeMessageRepository messageRepository,
                              DisputeEvidenceRepository evidenceRepository,
                              ContractRepository contractRepository,
                              MilestoneRepository milestoneRepository,
                              DisputeMapper mapper,
                              CurrentUser currentUser) {
        this.disputeRepository = disputeRepository;
        this.messageRepository = messageRepository;
        this.evidenceRepository = evidenceRepository;
        this.contractRepository = contractRepository;
        this.milestoneRepository = milestoneRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    // ================================================================
    // OPEN DISPUTE
    // ================================================================
    @Override
    @Transactional
    public DisputeResponse openDispute(Long contractId, OpenDisputeRequest req) {
        User caller = currentUser.require();

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + contractId));

        assertParticipant(contract, caller);

        if (contract.getStatus() == ContractStatus.COMPLETED
                || contract.getStatus() == ContractStatus.CANCELLED) {
            throw new IllegalStateException("Cannot open a dispute on a " + contract.getStatus() + " contract");
        }

        Milestone milestone = null;
        if (req.milestoneId() != null) {
            milestone = milestoneRepository.findById(req.milestoneId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Milestone not found with id " + req.milestoneId()));
            if (!milestone.getContract().getId().equals(contract.getId())) {
                throw new IllegalArgumentException("Milestone does not belong to this contract");
            }
        }

        // Enforce "at most one OPEN dispute per contract" — matches the DB unique index
        if (disputeRepository.findOpenForContract(contractId).isPresent()) {
            throw new IllegalStateException(
                    "A dispute is already OPEN on this contract. Resolve or withdraw it first.");
        }
        if (milestone != null && disputeRepository.findOpenForMilestone(milestone.getId()).isPresent()) {
            throw new IllegalStateException(
                    "A dispute is already OPEN on this milestone.");
        }

        Dispute d = new Dispute();
        d.setContract(contract);
        d.setMilestone(milestone);
        d.setOpenedBy(caller);
        d.setReason(req.reason().trim());
        d.setStatus(DisputeStatus.OPEN);
        Dispute saved = disputeRepository.save(d);

        // Mark contract as disputed (blocks milestone approval)
        contract.setDisputedAt(Instant.now());
        contractRepository.save(contract);

        log.info("Dispute {} opened on contract {} by user {}", saved.getId(), contractId, caller.getId());

        return toFull(saved);
    }

    // ================================================================
    // LIST / FIND
    // ================================================================
    @Override
    public List<DisputeSummaryResponse> listForContract(Long contractId) {
        User caller = currentUser.require();
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id " + contractId));
        assertParticipant(contract, caller);

        return disputeRepository.findByContractIdOrderByCreatedAtDesc(contractId)
                .stream().map(mapper::toSummary).toList();
    }

    @Override
    public DisputeResponse findById(Long disputeId) {
        User caller = currentUser.require();
        Dispute d = loadDispute(disputeId);
        assertParticipant(d.getContract(), caller);
        return toFull(d);
    }

    // ================================================================
    // MESSAGES
    // ================================================================
    @Override
    @Transactional
    public DisputeResponse addMessage(Long disputeId, DisputeMessageCreateRequest req) {
        User caller = currentUser.require();
        Dispute d = loadDispute(disputeId);
        assertParticipant(d.getContract(), caller);
        assertNotResolved(d);

        DisputeMessage m = new DisputeMessage();
        m.setDispute(d);
        m.setAuthor(caller);
        m.setMessage(req.message().trim());
        m.setFromAdmin(false);
        messageRepository.save(m);

        return toFull(d);
    }

    // ================================================================
    // EVIDENCE
    // ================================================================
    @Override
    @Transactional
    public DisputeResponse addEvidence(Long disputeId, DisputeEvidenceCreateRequest req) {
        User caller = currentUser.require();
        Dispute d = loadDispute(disputeId);
        assertParticipant(d.getContract(), caller);
        assertNotResolved(d);

        DisputeEvidenceType type = parseEvidenceType(req.evidenceType());

        DisputeEvidence e = new DisputeEvidence();
        e.setDispute(d);
        e.setUploadedBy(caller);
        e.setEvidenceType(type);
        e.setUrl(req.url().trim());
        e.setDescription(req.description() != null ? req.description().trim() : null);
        evidenceRepository.save(e);

        return toFull(d);
    }

    // ================================================================
    // WITHDRAW
    // ================================================================
    @Override
    @Transactional
    public DisputeResponse withdraw(Long disputeId) {
        User caller = currentUser.require();
        Dispute d = loadDispute(disputeId);
        if (!d.getOpenedBy().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the person who opened the dispute can withdraw it");
        }
        if (d.getStatus() != DisputeStatus.OPEN && d.getStatus() != DisputeStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Dispute is in terminal status " + d.getStatus() + " and cannot be withdrawn");
        }

        d.setStatus(DisputeStatus.WITHDRAWN);
        d.setResolvedAt(Instant.now());
        d.setResolution("Withdrawn by opener");
        disputeRepository.save(d);

        clearDisputedIfNoBlockingRemain(d.getContract());
        return toFull(d);
    }

    // ================================================================
    // ADMIN
    // ================================================================
    @Override
    public Page<DisputeSummaryResponse> adminList(DisputeStatus status, Pageable pageable) {
        Page<Dispute> page = (status != null)
                ? disputeRepository.findByStatus(status, pageable)
                : disputeRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(mapper::toSummary);
    }

    @Override
    @Transactional
    public DisputeResponse adminAddMessage(Long disputeId, DisputeMessageCreateRequest req) {
        User caller = currentUser.require();
        Dispute d = loadDispute(disputeId);
        assertNotResolved(d);

        DisputeMessage m = new DisputeMessage();
        m.setDispute(d);
        m.setAuthor(caller);
        m.setMessage(req.message().trim());
        m.setFromAdmin(true);
        messageRepository.save(m);

        // Flip OPEN → UNDER_REVIEW on first admin message
        if (d.getStatus() == DisputeStatus.OPEN) {
            d.setStatus(DisputeStatus.UNDER_REVIEW);
            disputeRepository.save(d);
        }
        return toFull(d);
    }

    @Override
    @Transactional
    public DisputeResponse adminResolve(Long disputeId, DisputeResolutionRequest req) {
        User admin = currentUser.require();
        Dispute d = loadDispute(disputeId);
        assertNotResolved(d);

        DisputeStatus newStatus = parseResolution(req.decision());

        d.setStatus(newStatus);
        d.setResolution(req.resolution().trim());
        d.setResolvedBy(admin);
        d.setResolvedAt(Instant.now());
        disputeRepository.save(d);

        // If this dispute no longer blocks, check whether all disputes on the contract are resolved.
        if (!newStatus.equals(DisputeStatus.OPEN)
                && !newStatus.equals(DisputeStatus.UNDER_REVIEW)
                && !newStatus.equals(DisputeStatus.ESCALATED)) {
            clearDisputedIfNoBlockingRemain(d.getContract());
        }

        log.info("Dispute {} resolved as {} by admin {}", disputeId, newStatus, admin.getId());
        return toFull(d);
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private Dispute loadDispute(Long id) {
        return disputeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found with id " + id));
    }

    private void assertParticipant(Contract c, User u) {
        boolean isClient = c.getClient().getId().equals(u.getId());
        boolean isFreelancer = c.getFreelancer().getId().equals(u.getId());
        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException("Not your contract");
        }
    }

    private void assertNotResolved(Dispute d) {
        switch (d.getStatus()) {
            case RESOLVED_FOR_CLIENT, RESOLVED_FOR_FREELANCER, WITHDRAWN -> {
                throw new IllegalStateException("Dispute is in terminal status " + d.getStatus());
            }
            default -> { /* OPEN, UNDER_REVIEW, ESCALATED — still active */ }
        }
    }

    private void clearDisputedIfNoBlockingRemain(Contract contract) {
        if (!disputeRepository.existsBlockingForContract(contract.getId())) {
            contract.setDisputedAt(null);
            contractRepository.save(contract);
        }
    }

    private DisputeEvidenceType parseEvidenceType(String s) {
        try {
            return DisputeEvidenceType.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid evidence type. Allowed: SCREENSHOT, FILE, LINK, OTHER");
        }
    }

    private DisputeStatus parseResolution(String s) {
        DisputeStatus status;
        try {
            status = DisputeStatus.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid decision. Allowed: RESOLVED_FOR_CLIENT, RESOLVED_FOR_FREELANCER, WITHDRAWN, ESCALATED");
        }
        switch (status) {
            case RESOLVED_FOR_CLIENT, RESOLVED_FOR_FREELANCER, WITHDRAWN, ESCALATED -> { return status; }
            default -> throw new IllegalArgumentException(
                    "Invalid decision. Allowed: RESOLVED_FOR_CLIENT, RESOLVED_FOR_FREELANCER, WITHDRAWN, ESCALATED");
        }
    }

    private DisputeResponse toFull(Dispute d) {
        List<DisputeMessage> messages = messageRepository.findByDisputeIdOrderByCreatedAtAsc(d.getId());
        List<DisputeEvidence> evidence = evidenceRepository.findByDisputeIdOrderByCreatedAtAsc(d.getId());
        return mapper.toFull(d, messages, evidence);
    }
}
