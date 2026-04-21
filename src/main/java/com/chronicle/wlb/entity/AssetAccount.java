package com.chronicle.wlb.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Asset_Accounts")
@Data
public class AssetAccount {

    @Id
    @Column(name = "account_id", length = 36, nullable = false)
    private String accountId;

    @Column(name = "identity_id", length = 36, nullable = false)
    private String identityId;

    @Column(name = "account_name", length = 100, nullable = false)
    private String accountName;

    @Column(name = "account_type", length = 20, nullable = false)
    private String accountType;

    @Column(name = "balance", precision = 10, scale = 2)
    private BigDecimal balance; //不使用 double，以免浮點樹精度出問題

    @Version // 🌟 啟動樂觀鎖機制！防禦高併發連點！
    @Column(name = "version")
    private Integer version;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        // 🌟 裝上監視器：只要這段有被執行，Console 就一定會印出這句話！
        System.out.println("🚨 [JPA 攔截器觸發] 準備更新 AssetAccount，已填上時間：" + this.updatedAt);
    }
}