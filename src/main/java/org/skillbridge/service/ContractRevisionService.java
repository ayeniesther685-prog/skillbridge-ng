package org.skillbridge.service;

import org.skillbridge.dto.request.RevisionRequestCreateRequest;
import org.skillbridge.dto.request.RevisionResponseUpdateRequest;
import org.skillbridge.dto.response.RevisionResponse;

import java.util.List;

public interface ContractRevisionService {

    /**
     * CLIENT requests a revision on a SUBMITTED milestone.
     *
     * Constraints enforced:
     *  - Only the contract's client may request
     *  - Milestone must be in SUBMITTED status
     *  - No other OPEN revision may exist for the milestone
     *  - If `isNewRequirements=true`, the request is recorded as an audit row
     *    with status ESCALATED_AS_NEW_REQUIREMENT and does NOT consume a
     *    revision slot; a `NewRequirementsException` is thrown to instruct
     *    the client to create a new milestone
     *  - Otherwise, revisionCount must be < revisionLimit; each accepted
     *    request increments the counter and moves the milestone to
     *    REVISION_REQUESTED
     */
    RevisionResponse requestRevision(Long milestoneId, RevisionRequestCreateRequest req);

    /**
     * FREELANCER responds to an OPEN revision request.
     *
     * accept=true  -> ACCEPTED. Milestone goes back to IN_PROGRESS so the
     *                 freelancer can do the revision and resubmit.
     * accept=false -> REJECTED. Milestone stays SUBMITTED; client can approve
     *                 as-is or escalate as new requirements.
     */
    RevisionResponse respondToRevision(Long revisionId, RevisionResponseUpdateRequest req);

    /** List all revisions for a contract, newest first. Client OR freelancer. */
    List<RevisionResponse> listForContract(Long contractId);

    /** List all revisions for a specific milestone. Client OR freelancer. */
    List<RevisionResponse> listForMilestone(Long milestoneId);

    /** Fetch a single revision. Client OR freelancer on the parent contract. */
    RevisionResponse findById(Long revisionId);
}
