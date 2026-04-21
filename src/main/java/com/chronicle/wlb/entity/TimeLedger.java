package com.chronicle.wlb.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Time_Ledgers")
@Data
public class TimeLedger {

    @Id
    @Column(name = "ledger_id", length = 36, nullable = false)
    private String ledgerId;

    @Column(name = "identity_id", length = 36, nullable = false)
    private String identityId;

    @Column(name = "from_account_id", length = 36, nullable = false)
    private String fromAccountId;

    @Column(name = "to_account_id", length = 36, nullable = false)
    private String toAccountId;

    @Column(name = "execution_id", length = 36, nullable = false)
    private String executionId;

    @Column(name = "time_spent", nullable = false)
    private Integer timeSpent;

    @Column(name = "asset_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal assetAmount;

    @Column(name = "reflection_memo", columnDefinition = "TEXT")
    private String reflectionMemo;

    @Column(name = "album_image_url", length = 255)
    private String AlbumImageUrl;

    // 🌟 加上 updatable = false，這在程式碼層面強制規定：這筆時間一旦寫入，任何人都不准改！
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🌟 讓收據在存入金庫的那一瞬間，自動蓋上時間戳章！
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // (雖然我們架構上不允許修改收據，但為了防呆，我們還是把這個加上去)
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}