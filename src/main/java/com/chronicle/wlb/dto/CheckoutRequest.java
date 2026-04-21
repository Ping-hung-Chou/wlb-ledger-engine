package com.chronicle.wlb.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CheckoutRequest {
    private String fromAccountId;  // 來源帳戶 ID (可用時間池)
    private String toAccountId;    // 目標帳戶 ID (充電資產)
    private String executionId;    // 任務/活動 ID (例如：畫漫畫的 ID)
    private BigDecimal timeSpent;  // 花費的時間 (金額)
    private Integer sourceVersion; // 🛡️ 核心防禦：來源帳戶的樂觀鎖版本號
}