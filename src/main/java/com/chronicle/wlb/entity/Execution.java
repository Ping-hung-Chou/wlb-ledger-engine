package com.chronicle.wlb.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "Executions")
@Data
public class Execution {

    @Id
    @Column(name = "execution_id", length = 36, nullable = false)
    private String executionId;

    @Column(name = "activity_name", length = 100, nullable = false)
    private String activityName;

    @Column(name = "conversion_multiplier", precision = 5, scale = 2)
    private BigDecimal conversionMultiplier;

    @Column(name = "is_active")
    private Boolean isActive;
}