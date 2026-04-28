package com.chronicle.wlb.entity.enums;

/**
 * Represents the lifecycle states of a MilestoneNode.
 * Valid state transitions:
 *   PENDING → IN_PROGRESS → PAUSED → IN_PROGRESS → COMPLETED → ARCHIVED
 */
public enum MilestoneStatus {
    PENDING,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    ARCHIVED
}
