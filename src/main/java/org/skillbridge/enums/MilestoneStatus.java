package org.skillbridge.enums;

import java.util.Set;

public enum MilestoneStatus {
    PENDING,
    IN_PROGRESS,
    SUBMITTED,
    REVISION_REQUESTED,
    COMPLETED,
    APPROVED;

    public Set<MilestoneStatus> allowedNext() {
        return switch (this) {
            case PENDING            -> Set.of(IN_PROGRESS);
            case IN_PROGRESS        -> Set.of(SUBMITTED);
            case SUBMITTED          -> Set.of(APPROVED, REVISION_REQUESTED);
            case REVISION_REQUESTED -> Set.of(IN_PROGRESS);
            case COMPLETED          -> Set.of(APPROVED);   // legacy compatibility
            case APPROVED           -> Set.of();
        };
    }

    public boolean isTerminal() {
        return this == APPROVED;
    }
}
