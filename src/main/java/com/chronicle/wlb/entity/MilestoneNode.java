package com.chronicle.wlb.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Milestone_Nodes")
@Data
public class MilestoneNode {

    @Id
    @Column(name = "node_id", length = 36, nullable = false)
    private String nodeId;

    @Column(name = "identity_id", length = 36, nullable = false)
    private String identityId;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}