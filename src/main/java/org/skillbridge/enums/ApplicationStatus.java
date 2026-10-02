package org.skillbridge.enums;

import java.util.Set;

public enum ApplicationStatus {
    PENDING,
    SHORTLISTED,
    ACCEPTED,
    REJECTED,
    WITHDRAWN;

    /**
     * Valid transitions from this status.
     * Used to enforce the state machine in ApplicationServiceImpl.
     */
    public Set<ApplicationStatus> allowedNext() {
        return switch (this) {
            case PENDING     -> Set.of(SHORTLISTED, REJECTED, WITHDRAWN);
            case SHORTLISTED -> Set.of(ACCEPTED, REJECTED, WITHDRAWN);
            case ACCEPTED    -> Set.of();
            case REJECTED    -> Set.of();
            case WITHDRAWN   -> Set.of();
        };
    }

    public boolean isTerminal() {
        return allowedNext().isEmpty();
    }
}