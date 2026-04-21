package com.chronicle.wlb.service;

import com.chronicle.wlb.dto.CheckoutRequest;
import com.chronicle.wlb.entity.AssetAccount;
import com.chronicle.wlb.entity.TimeLedger;
import com.chronicle.wlb.repository.AssetAccountRepository;
import com.chronicle.wlb.repository.TimeLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor // 🌟 潔癖架構：使用 final 綁定專員
public class LedgerService {

    private final AssetAccountRepository accountRepo;
    private final TimeLedgerRepository ledgerRepo;

    @Transactional(rollbackFor = Exception.class) // 🛡️ 核武級防禦：錯一步就全部 Rollback
    public void processCheckout(CheckoutRequest request) {

        // 1. 找出來源與目標帳戶
        AssetAccount sourceAccount = accountRepo.findById(request.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Can not find source account!"));
        AssetAccount targetAccount = accountRepo.findById(request.getToAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Can not find target account!"));

        // 2. 🛡️ 樂觀鎖比對 (Optimistic Lock Check)
        // 檢查 Wojak 手機上的 version，是不是跟金庫裡最新的 version 一樣？
        if (!sourceAccount.getVersion().equals(request.getSourceVersion())) {
            throw new IllegalStateException("Trade failed：Optimistic Lock Triggered (帳戶狀態已改變，請重新整理後再試！)");
        }

        // 3. 檢查餘額是否充足 (這裡假設來源帳戶不能扣到負數)
        // 注意：如果是「可用時間」，初期可能是 0，你可以依據遊戲規則決定要不要擋。
        // 這裡我們先示範防禦邏輯：
        // if (sourceAccount.getBalance().compareTo(request.getTimeSpent()) < 0) {
        //     throw new IllegalArgumentException("餘額不足！");
        // }

        // 4. 執行雙式記帳 (扣款與入帳)
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getTimeSpent()));

        // 🌟 敏捷開發替身術 (Mocking)：
        // 假設我們未來會去 Activity 表查到「畫漫畫」的倍率是 1.5 倍
        // TODO: 未來實作 Activity 模組後，這裡要改成從 DB 撈取倍率
        BigDecimal mockMultiplier = new BigDecimal("1.5");

        // 結算資產 = 花費時間 * 倍率
        BigDecimal finalAssetAmount = request.getTimeSpent().multiply(mockMultiplier);

        // ... 更新目標帳戶餘額 (這裡也要記得用乘完的數字喔！)
        targetAccount.setBalance(targetAccount.getBalance().add(finalAssetAmount));

        // ⚠️ 我們不寫 sourceAccount.setVersion(...)！讓 JPA 自動處理！
        accountRepo.save(sourceAccount);
        accountRepo.save(targetAccount);

        // 5. 寫入不可竄改的歷史軌跡
        TimeLedger ledgerRecord = new TimeLedger();
        ledgerRecord.setLedgerId(UUID.randomUUID().toString());
        ledgerRecord.setExecutionId(request.getExecutionId());
        ledgerRecord.setIdentityId(sourceAccount.getIdentityId());
        ledgerRecord.setFromAccountId(sourceAccount.getAccountId());
        ledgerRecord.setToAccountId(targetAccount.getAccountId());

        // 🌟 完美分流：時間歸時間，資產歸資產！
        ledgerRecord.setTimeSpent(request.getTimeSpent().intValue()); // 如果 Entity 裡是 int，記得轉型 (假設傳進來的是秒數)
        ledgerRecord.setAssetAmount(finalAssetAmount); // 存入乘上 1.5 倍後的金流

        ledgerRepo.save(ledgerRecord);
    }
}