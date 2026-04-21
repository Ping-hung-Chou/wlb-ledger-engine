package com.chronicle.wlb.service;

import com.chronicle.wlb.entity.TimeLedger;
import com.chronicle.wlb.repository.TimeLedgerRepository; // 🌟 記得 import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    // 🌟 注入金庫軌跡管理員
    private final TimeLedgerRepository timeLedgerRepo;

    @Scheduled(fixedRate = 10000)
    public void generateWeeklyReportTask() {
        log.info("==================================================");
        log.info("⏰ [AI Report] start time：{}", LocalDateTime.now());

        // 🌟 這裡請填入你之前截圖裡，Wojak 真實的 identity_id！
        String wojakIdentityId = "9d64e251-80e5-4e20-9ba0-33cc518b929c";

        // 1. 資料聚合 (Data Aggregation)：去金庫把 Wojak 的帳本全部抱上來
        List<TimeLedger> ledgers = timeLedgerRepo.findByIdentityId(wojakIdentityId);

        // 使用 Java Stream 魔法，瞬間加總所有花費的時間與獲得的資產
        double totalTimeSpent = ledgers.stream()
                .mapToDouble(ledger -> ledger.getTimeSpent().doubleValue())
                .sum();

        double totalAssetEarned = ledgers.stream()
                .mapToDouble(ledger -> ledger.getAssetAmount().doubleValue())
                .sum();

        // 動態組裝 Context Stuffing
        String realContext = String.format("User Wojak has invested a total of %.2f hours this week, earning %.2f hours of Leisure Assets.",
                totalTimeSpent, totalAssetEarned);

        log.info("🔍 Task 1: Aggregated real data Context -> {}", realContext); //已聚合真實數據 Context

        // 2. 呼叫 LLM 與容錯降級防禦
        boolean fallbackUsed = false;
        String finalInsight;

        try {
            log.info("🌐 Task 2: Preparing to call external LLM API...");
            finalInsight = callLlmApi(realContext);
        } catch (Exception e) {
            log.warn("🚨 Warning! LLM connection failed:{}", e.getMessage());

            // 🌟 將真實數據塞入降級金句中，讓 Fallback 依然具有高度個人化！
            finalInsight = String.format("[System Fallback Message] Great effort this week! Although your AI mentor went for a coffee break, the %.2f hours you invested have been securely recorded in the Chronicle. Keep up this focus next week!", totalTimeSpent);
            fallbackUsed = true;
        }

        log.info("📝 Final generated report (Fallback status: {})：\n{}", fallbackUsed, finalInsight);
        log.info("==================================================\n");
    }

    private String callLlmApi(String context) {
        throw new RuntimeException("HTTP 429 Too Many Requests - Simulated LLM API failure");
    }
}