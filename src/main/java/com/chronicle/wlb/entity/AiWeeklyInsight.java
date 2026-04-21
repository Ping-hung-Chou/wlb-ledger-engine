package com.chronicle.wlb.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "AI_Weekly_Insights")
@Data
public class AiWeeklyInsight {

    @Id
    @Column(name = "insight_id", length = 36, nullable = false)
    private String insightId;

    @Column(name = "identity_id", length = 36, nullable = false)
    private String identityId;

    @Column(name = "year_week", length = 10, nullable = false)
    private String yearWeek;

    @Column(name = "ai_summary", columnDefinition = "TEXT", nullable = false)
    private String aiSummary;

    @Column(name = "token_used")
    private Integer tokenUsed;

    @Column(name = "fallback_used")
    private Boolean fallbackUsed;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}