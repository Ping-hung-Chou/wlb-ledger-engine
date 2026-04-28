package com.chronicle.wlb.entity;

import com.chronicle.wlb.entity.enums.MilestoneStatus;
import com.chronicle.wlb.entity.enums.MilestoneType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for the Milestone_Nodes table.
 * Acts as the core goal-management unit, tracking the full lifecycle
 * and time investment of both WORK and LEISURE milestones.
 */
@Entity
@Table(name = "Milestone_Nodes")
@Data
public class MilestoneNode {

    // Primary key: UUID string (36 chars), auto-generated in @PrePersist — never set by callers.
    @Id
    @Column(name = "node_id", length = 36, nullable = false)
    private String nodeId;

    // FK reference to Identities.identity_id — stored as a plain string to avoid tight coupling.
    @Column(name = "identity_id", length = 36, nullable = false)
    private String identityId;

    // Human-readable title for the milestone goal.
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    // Optional free-text description providing additional context for the goal.
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Domain category of this milestone: WORK or LEISURE — persisted as a STRING enum.
    @Enumerated(EnumType.STRING)
    @Column(name = "milestone_type", nullable = false)
    private MilestoneType milestoneType;

    // Current lifecycle state — persisted as a STRING enum; initialized to PENDING in @PrePersist.
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MilestoneStatus status;

    // Planned time commitment for this milestone, in minutes.
    @Column(name = "target_minutes", nullable = false)
    private Integer targetMinutes;

    // Cumulative actual time invested, in minutes — initialized to 0 in @PrePersist.
    @Column(name = "actual_minutes")
    private Integer actualMinutes;

    // Asset conversion multiplier applied during ledger checkout — defaults to 1.00 (no adjustment).
    @Column(name = "time_multiplier", precision = 4, scale = 2)
    private BigDecimal timeMultiplier;

    // Immutable record creation timestamp — blocked from updates via updatable=false.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Timestamp set when the milestone transitions to IN_PROGRESS for the first time.
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    // Timestamp set when the milestone transitions to COMPLETED.
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Timestamp of the most recent PAUSED state transition.
    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    /**
     * Lifecycle hook executed before the first INSERT.
     * Guarantees safe default values without relying on the caller:
     * - nodeId       : randomly generated UUID (36-char string)
     * - createdAt    : current server time
     * - actualMinutes: 0 (no time invested yet)
     * - timeMultiplier: 1.00 (neutral — no inflation or deflation)
     * - status       : PENDING (milestone not yet started)
     */
    @PrePersist
    private void prePersist() {
        this.nodeId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.actualMinutes = 0;
        this.timeMultiplier = new BigDecimal("1.00");
        this.status = MilestoneStatus.PENDING;
    }
}
