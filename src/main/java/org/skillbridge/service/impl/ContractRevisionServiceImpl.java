package org.skillbridge.service.impl;

import org.skillbridge.dto.request.RevisionRequestCreateRequest;
import org.skillbridge.dto.request.RevisionResponseUpdateRequest;
import org.skillbridge.dto.response.RevisionResponse;
import org.skillbridge.entity.Contract;
import org.skillbridge.entity.ContractRevision;
import org.skillbridge.entity.Milestone;
import org.skillbridge.entity.User;
import org.skillbridge.enums.MilestoneStatus;
import org.skillbridge.enums.RevisionStatus;
import org.skillbridge.exception.NewRequirementsException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.exception.RevisionLimitExceededException;
import org.skillbridge.repository.ContractRevisionRepository;
import org.skillbridge.repository.MilestoneRepository;
import org.skillbridge.security.CurrentUser;
import org.skillbridge.service.ContractRevisionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContractRevisionServiceImpl implements ContractRevisionService {

    private final ContractRevisionRepository revisionRepository;
    private final MilestoneRepository milestoneRepository;
    private final CurrentUser currentUser;

    public ContractRevisionServiceImpl(ContractRevisionRepository revisionRepository,
                                       MilestoneRepository milestoneRepository,
                                       CurrentUser currentUser) {
        this.revisionRepository = revisionRepository;
        this.milestoneRepository = milestoneRepository;
        this.currentUser = currentUser;
    }

    // ---------------------------------------------------------------------
    // CLIENT: request revision
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public RevisionResponse requestRevision(Long milestoneId, RevisionRequestCreateRequest req) {
        User caller = currentUser.require();
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id " + milestoneId));

        Contract contract = milestone.getContract();
        if (!contract.getClient().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the contract's client can request a revision");
        }

        if (milestone.getStatus() != MilestoneStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Revisions can only be requested on SUBMITTED milestones. Current status: " + milestone.getStatus());
        }

        List<ContractRevision> open = revisionRepository.findOpenByMilestone(milestoneId);
        if (!open.isEmpty()) {
            throw new IllegalStateException(
                    "A revision request is already OPEN for this milestone. Wait for the freelancer to respond.");
        }

        // If the client is explicitly asking for NEW scope, record an audit
        // row but do NOT consume a revision slot. Instruct them to create a
        // new milestone instead.
        if (req.isNewRequirements()) {
            ContractRevision audit = buildRevision(milestone, caller, req.reason(), true);
            audit.setStatus(RevisionStatus.ESCALATED_AS_NEW_REQUIREMENT);
            audit.setResolvedAt(Instant.now());
            ContractRevision saved = revisionRepository.save(audit);
            throw new NewRequirementsException(
                    "This request is for scope outside the original milestone. " +
                    "Create a new milestone or a new contract instead. " +
                    "Audit record id=" + saved.getId());
        }

        // Enforce per-milestone revision limit.
        int limit = milestone.getRevisionLimit() != null ? milestone.getRevisionLimit() : 3;
        int count = milestone.getRevisionCount() != null ? milestone.getRevisionCount() : 0;
        if (count >= limit) {
            throw new RevisionLimitExceededException(
                    "Revision limit reached for this milestone (" + count + "/" + limit + "). " +
                    "If the work still does not meet expectations, open a dispute or create a new milestone.");
        }

        ContractRevision revision = buildRevision(milestone, caller, req.reason(), false);
        ContractRevision saved = revisionRepository.save(revision);

        // Update milestone state + counter
        milestone.setRevisionCount(count + 1);
        milestone.setStatus(MilestoneStatus.REVISION_REQUESTED);
        milestoneRepository.save(milestone);

        return toResponse(saved);
    }

    // ---------------------------------------------------------------------
    // FREELANCER: respond to revision
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public RevisionResponse respondToRevision(Long revisionId, RevisionResponseUpdateRequest req) {
        User caller = currentUser.require();
        ContractRevision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found with id " + revisionId));

        Milestone milestone = revision.getMilestone();
        Contract contract = milestone.getContract();
        if (!contract.getFreelancer().getId().equals(caller.getId())) {
            throw new AccessDeniedException("Only the contract's freelancer can respond to a revision");
        }

        if (revision.getStatus() != RevisionStatus.OPEN) {
            throw new IllegalStateException(
                    "Revision is not OPEN. Current status: " + revision.getStatus());
        }

        revision.setFreelancerResponse(req.response());
        revision.setResolvedAt(Instant.now());

        if (Boolean.TRUE.equals(req.accept())) {
            revision.setStatus(RevisionStatus.ACCEPTED);
            // Milestone goes back to IN_PROGRESS so the freelancer can do the work
            milestone.setStatus(MilestoneStatus.IN_PROGRESS);
        } else {
            revision.setStatus(RevisionStatus.REJECTED);
            // Milestone stays SUBMITTED; client can approve as-is
        }

        revisionRepository.save(revision);
        milestoneRepository.save(milestone);

        return toResponse(revision);
    }

    // ---------------------------------------------------------------------
    // READ
    // ---------------------------------------------------------------------
    @Override
    public List<RevisionResponse> listForContract(Long contractId) {
        User caller = currentUser.require();

        List<ContractRevision> revisions =
                revisionRepository.findByMilestoneContractIdOrderByCreatedAtDesc(contractId);

        // Verify ownership. Fail closed: even an empty revision list must not
        // leak contract existence to a non-participant.
        Contract contract = null;
        if (!revisions.isEmpty()) {
            contract = revisions.get(0).getMilestone().getContract();
        } else {
            List<Milestone> milestones = milestoneRepository.findByContractIdOrderByCreatedAtAsc(contractId);
            if (!milestones.isEmpty()) {
                contract = milestones.get(0).getContract();
            }
        }
        if (contract == null) {
            throw new ResourceNotFoundException("Contract not found with id " + contractId);
        }
        boolean isClient = contract.getClient().getId().equals(caller.getId());
        boolean isFreelancer = contract.getFreelancer().getId().equals(caller.getId());
        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException("Not your contract");
        }
        return revisions.stream().map(this::toResponse).toList();
    }

    @Override
    public List<RevisionResponse> listForMilestone(Long milestoneId) {
        User caller = currentUser.require();
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id " + milestoneId));
        Contract contract = milestone.getContract();
        boolean isClient = contract.getClient().getId().equals(caller.getId());
        boolean isFreelancer = contract.getFreelancer().getId().equals(caller.getId());
        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException("Not your contract");
        }
        return revisionRepository.findByMilestoneIdOrderByCreatedAtDesc(milestoneId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public RevisionResponse findById(Long revisionId) {
        User caller = currentUser.require();
        ContractRevision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found with id " + revisionId));
        Contract contract = revision.getMilestone().getContract();
        boolean isClient = contract.getClient().getId().equals(caller.getId());
        boolean isFreelancer = contract.getFreelancer().getId().equals(caller.getId());
        if (!isClient && !isFreelancer) {
            throw new AccessDeniedException("Not your contract");
        }
        return toResponse(revision);
    }

    // ---------------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------------
    private ContractRevision buildRevision(Milestone milestone, User requester,
                                           String reason, boolean isNewRequirements) {
        ContractRevision r = new ContractRevision();
        r.setMilestone(milestone);
        r.setRequestedBy(requester);
        r.setReason(reason);
        r.setStatus(RevisionStatus.OPEN);
        r.setNewRequirements(isNewRequirements);
        return r;
    }

    private RevisionResponse toResponse(ContractRevision r) {
        User requester = r.getRequestedBy();
        String name = requester != null
                ? (requester.getFirstName() + " " + requester.getLastName()).trim()
                : null;
        return new RevisionResponse(
                r.getId(),
                r.getMilestone().getId(),
                r.getMilestone().getContract().getId(),
                requester != null ? requester.getId() : null,
                name,
                r.getReason(),
                r.getStatus(),
                r.isNewRequirements(),
                r.getFreelancerResponse(),
                r.getResolvedAt(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
