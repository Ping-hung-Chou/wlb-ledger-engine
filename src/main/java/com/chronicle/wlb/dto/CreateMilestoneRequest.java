package com.chronicle.wlb.dto;

import com.chronicle.wlb.entity.enums.MilestoneType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for creating a new MilestoneNode.
 * Validated at the controller layer before being handed to the service.
 */
@Data
public class CreateMilestoneRequest {

    // Milestone title — required, max 100 characters.
    @NotBlank
    @Size(max = 100)
    private String title;

    // Optional free-text description providing additional context for the goal.
    private String description;

    // Domain category of this milestone: WORK or LEISURE — required.
    @NotNull
    private MilestoneType milestoneType;

    // Planned time commitment in minutes — must be at least 1.
    @Min(1)
    private int targetMinutes;
}
