package org.skillbridge.service;

import org.skillbridge.dto.request.DisputeEvidenceCreateRequest;
import org.skillbridge.dto.request.DisputeMessageCreateRequest;
import org.skillbridge.dto.request.DisputeResolutionRequest;
import org.skillbridge.dto.request.OpenDisputeRequest;
import org.skillbridge.dto.response.DisputeResponse;
import org.skillbridge.dto.response.DisputeSummaryResponse;
import org.skillbridge.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DisputeService {

    // ================= user-facing =================

    /**
     * Open a dispute. Caller must be CLIENT or FREELANCER on the contract.
     * - one OPEN dispute per contract (milestone-level or contract-level)
     * - one OPEN dispute per milestone if milestoneId is set
     * - cannot open on a COMPLETED or CANCELLED contract
     * - sets contract.disputedAt to block payment release
     */
    DisputeResponse openDispute(Long contractId, OpenDisputeRequest req);

    /** List disputes on a contract. Client OR freelancer on it. */
    List<DisputeSummaryResponse> listForContract(Long contractId);

    /** Full dispute detail with thread + evidence. Client OR freelancer on it. */
    DisputeResponse findById(Long disputeId);

    /** Post a message. Client OR freelancer on the contract. */
    DisputeResponse addMessage(Long disputeId, DisputeMessageCreateRequest req);

    /** Attach evidence URL. Client OR freelancer on the contract. */
    DisputeResponse addEvidence(Long disputeId, DisputeEvidenceCreateRequest req);

    /** Opener withdraws the dispute (only while still OPEN/UNDER_REVIEW). */
    DisputeResponse withdraw(Long disputeId);

    // ================= admin =================

    /** Paginated list, optionally filtered by status. */
    Page<DisputeSummaryResponse> adminList(DisputeStatus status, Pageable pageable);

    /** Admin posts a message (marked is_from_admin = true). */
    DisputeResponse adminAddMessage(Long disputeId, DisputeMessageCreateRequest req);

    /**
     * Admin resolves the dispute. `decision` must be one of
     * RESOLVED_FOR_CLIENT, RESOLVED_FOR_FREELANCER, WITHDRAWN, ESCALATED.
     * On terminal resolutions, clears contract.disputedAt.
     */
    DisputeResponse adminResolve(Long disputeId, DisputeResolutionRequest req);
}
