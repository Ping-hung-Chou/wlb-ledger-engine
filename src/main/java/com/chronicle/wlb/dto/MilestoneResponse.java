package com.chronicle.wlb.dto;

import com.chronicle.wlb.entity.enums.MilestoneStatus;
import com.chronicle.wlb.entity.enums.MilestoneType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read-only response payload returned to the client after any milestone operation.
 * Mirrors the MilestoneNode entity fields; no business logic resides here.
 */
@Data
public class MilestoneResponse {

    private String nodeId;
    private String identityId;
    private String title;
    private String description;

    private MilestoneType milestoneType;
    private MilestoneStatus status;

    private int targetMinutes;
    private int actualMinutes;

    // Asset conversion multiplier — reflects any time inflation or deflation applied.
    private BigDecimal timeMultiplier;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime pausedAt;
}
