package com.chronicle.wlb.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity // 告訴 Spring 這是資料庫的替身
@Table(name = "Identities") // 精準對應到我們剛剛建的表名
@Data // 這是 Lombok 魔法，自動幫我們產生 Getter/Setter
public class Identity {

    @Id // 標記這是 Primary Key
    @Column(name = "identity_id", length = 36, nullable = false)
    private String identityId; // Java 習慣用小駝峰命名

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", columnDefinition = "JSON")
    private String role; // JSON 格式在 Java 先用 String 接住

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}