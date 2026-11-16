package org.skillbridge.service.impl;

import org.skillbridge.dto.request.ProviderVerificationSubmitRequest;
import org.skillbridge.dto.response.ProviderVerificationEventResponse;
import org.skillbridge.dto.response.ProviderVerificationResponse;
import org.skillbridge.dto.response.ProviderVerificationStatusResponse;
import org.skillbridge.entity.ProviderVerification;
import org.skillbridge.entity.ProviderVerificationEvent;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ProviderType;
import org.skillbridge.enums.VerificationEventType;
import org.skillbridge.enums.VerificationStatus;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.ProviderVerificationMapper;
import org.skillbridge.repository.ProviderVerificationEventRepository;
import org.skillbridge.repository.ProviderVerificationRepository;
import org.skillbridge.security.CurrentUser;
import org.skillbridge.service.ProviderVerificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProviderVerificationServiceImpl implements ProviderVerificationService {

    private final ProviderVerificationRepository repo;
    private final ProviderVerificationEventRepository eventRepo;
    private final ProviderVerificationMapper mapper;
    private final CurrentUser currentUser;

    public ProviderVerificationServiceImpl(
            ProviderVerificationRepository repo,
            ProviderVerificationEventRepository eventRepo,
            ProviderVerificationMapper mapper,
            CurrentUser currentUser
    ) {
        this.repo = repo;
        this.eventRepo = eventRepo;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    // ======================= user-facing =======================

    @Override
    public ProviderVerificationResponse getMine() {
        User me = currentUser.require();
        ProviderVerification pv = repo.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No verification profile yet"));
        return mapper.toResponse(pv);
    }

    @Override
    public ProviderVerificationStatusResponse getStatusForUser(Long userId) {
        ProviderVerification pv = repo.findByUserId(userId)
                .orElse(null);
        if (pv == null) {
            return new ProviderVerificationStatusResponse(userId, VerificationStatus.UNVERIFIED, false);
        }
        return new ProviderVerificationStatusResponse(
                userId, pv.getStatus(), pv.getStatus() == VerificationStatus.VERIFIED);
    }

    @Override
    @Transactional
    public ProviderVerificationResponse submit(ProviderVerificationSubmitRequest req) {
        User me = currentUser.require();

        ProviderVerification pv = repo.findByUserId(me.getId()).orElseGet(() -> {
            ProviderVerification fresh = new ProviderVerification();
            fresh.setUser(me);
            return fresh;
        });

        // Guard: cannot resubmit while already PENDING or VERIFIED (must be rejected/suspended/expired first)
        if (pv.getStatus() == VerificationStatus.PENDING) {
            throw new IllegalStateException("Verification already pending review");
        }
        if (pv.getStatus() == VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Provider is already verified");
        }

        VerificationStatus fromStatus = pv.getStatus();

        pv.setProviderType(req.providerType());
        pv.setLegalName(req.legalName());
        pv.setCacRegistrationNumber(req.cacRegistrationNumber());
        pv.setOfficialEmail(req.officialEmail());
        pv.setOfficialPhone(req.officialPhone());
        pv.setPhysicalAddress(req.physicalAddress());
        pv.setWebsite(req.website());
        pv.setLinkedinUrl(req.linkedinUrl());
        pv.setAuthorizedRepName(req.authorizedRepName());
        pv.setAuthorizedRepRole(req.authorizedRepRole());
        pv.setStatus(VerificationStatus.PENDING);
        pv.setSubmittedAt(Instant.now());
        pv.setVerifiedAt(null);
        pv.setVerifiedByAdmin(null);

        ProviderVerification saved = repo.save(pv);
        logEvent(saved, VerificationEventType.SUBMITTED, me, fromStatus, VerificationStatus.PENDING, "Submitted by user");

        return mapper.toResponse(saved);
    }

    // ======================= admin =======================

    @Override
    public Page<ProviderVerificationResponse> adminList(VerificationStatus status, ProviderType type, Pageable pageable) {
        Page<ProviderVerification> page;
        if (status != null && type != null) {
            page = repo.findByStatusAndProviderType(status, type, pageable);
        } else if (status != null) {
            page = repo.findByStatus(status, pageable);
        } else {
            page = repo.findAll(pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Override
    public ProviderVerificationResponse adminGet(Long id) {
        ProviderVerification pv = load(id);
        return mapper.toResponse(pv);
    }

    @Override
    public List<ProviderVerificationEventResponse> adminEvents(Long id) {
        load(id);
        return eventRepo.findByVerificationIdOrderByCreatedAtDesc(id)
                .stream().map(mapper::toEventResponse).toList();
    }

    @Override
    @Transactional
    public ProviderVerificationResponse adminApprove(Long id, String reason) {
        ProviderVerification pv = load(id);
        if (pv.getStatus() == VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Already verified");
        }
        if (pv.getStatus() != VerificationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING verifications can be approved");
        }
        User admin = currentUser.require();
        VerificationStatus from = pv.getStatus();
        pv.setStatus(VerificationStatus.VERIFIED);
        pv.setVerifiedAt(Instant.now());
        pv.setVerifiedByAdmin(admin);
        pv.setAdminNotes(reason);
        pv.setExpiresAt(Instant.now().plusSeconds(365L * 24 * 3600)); // 1-year validity, configurable later
        ProviderVerification saved = repo.save(pv);
        logEvent(saved, VerificationEventType.APPROVED, admin, from, VerificationStatus.VERIFIED, reason);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProviderVerificationResponse adminReject(Long id, String reason) {
        ProviderVerification pv = load(id);
        if (pv.getStatus() != VerificationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING verifications can be rejected");
        }
        User admin = currentUser.require();
        VerificationStatus from = pv.getStatus();
        pv.setStatus(VerificationStatus.REJECTED);
        pv.setAdminNotes(reason);
        ProviderVerification saved = repo.save(pv);
        logEvent(saved, VerificationEventType.REJECTED, admin, from, VerificationStatus.REJECTED, reason);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProviderVerificationResponse adminSuspend(Long id, String reason) {
        ProviderVerification pv = load(id);
        if (pv.getStatus() == VerificationStatus.SUSPENDED) {
            throw new IllegalStateException("Already suspended");
        }
        User admin = currentUser.require();
        VerificationStatus from = pv.getStatus();
        pv.setStatus(VerificationStatus.SUSPENDED);
        pv.setAdminNotes(reason);
        ProviderVerification saved = repo.save(pv);
        logEvent(saved, VerificationEventType.SUSPENDED, admin, from, VerificationStatus.SUSPENDED, reason);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProviderVerificationResponse adminRevoke(Long id, String reason) {
        ProviderVerification pv = load(id);
        if (pv.getStatus() == VerificationStatus.UNVERIFIED) {
            throw new IllegalStateException("Already unverified");
        }
        User admin = currentUser.require();
        VerificationStatus from = pv.getStatus();
        pv.setStatus(VerificationStatus.UNVERIFIED);
        pv.setVerifiedAt(null);
        pv.setVerifiedByAdmin(null);
        pv.setAdminNotes(reason);
        ProviderVerification saved = repo.save(pv);
        logEvent(saved, VerificationEventType.REVOKED, admin, from, VerificationStatus.UNVERIFIED, reason);
        return mapper.toResponse(saved);
    }

    @Override
    public boolean isVerified(Long userId) {
        return repo.findByUserId(userId)
                .map(pv -> pv.getStatus() == VerificationStatus.VERIFIED)
                .orElse(false);
    }

    // ======================= helpers =======================

    private ProviderVerification load(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found with id " + id));
    }

    private void logEvent(ProviderVerification pv,
                          VerificationEventType type,
                          User actor,
                          VerificationStatus from,
                          VerificationStatus to,
                          String notes) {
        ProviderVerificationEvent ev = new ProviderVerificationEvent();
        ev.setVerification(pv);
        ev.setEventType(type);
        ev.setActor(actor);
        ev.setFromStatus(from);
        ev.setToStatus(to);
        ev.setNotes(notes);
        eventRepo.save(ev);
    }
}