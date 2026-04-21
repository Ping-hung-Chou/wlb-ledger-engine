package com.chronicle.wlb.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "System_Configs")
@Data
public class SystemConfig {

    @Id // 這個的主鍵不是 UUID，是字串 key！
    @Column(name = "config_key", length = 50, nullable = false)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT", nullable = false)
    private String configValue;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}