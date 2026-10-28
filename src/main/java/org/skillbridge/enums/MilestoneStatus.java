package org.skillbridge.enums;

import java.util.Set;

public enum MilestoneStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    APPROVED;

    public Set<MilestoneStatus> allowedNext() {
        return switch (this) {
            case PENDING     -> Set.of(IN_PROGRESS);
            case IN_PROGRESS -> Set.of(COMPLETED);
            case COMPLETED   -> Set.of(APPROVED);
            case APPROVED    -> Set.of();
        };
    }

    public boolean isTerminal() {
        return allowedNext().isEmpty();
    }
}