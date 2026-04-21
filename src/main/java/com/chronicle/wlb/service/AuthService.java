package com.chronicle.wlb.service;

import com.chronicle.wlb.dto.RegisterRequest;
import com.chronicle.wlb.entity.AssetAccount;
import com.chronicle.wlb.entity.Identity;
import com.chronicle.wlb.repository.AssetAccountRepository;
import com.chronicle.wlb.repository.IdentityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.UUID;

// 引入 Lombok
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // 🌟 核心魔法：Lombok 會自動幫所有標記為 final 的變數建立「建構子合約」！
public class AuthService {

    private final IdentityRepository identityRepo;
    private final AssetAccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class) // 核心防禦：開戶與發放初始錢包，必須同時成功或同時失敗
    public Identity registerUser(RegisterRequest request) {

        // 1. 檢查帳號是否已被註冊
        if (identityRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("This account has been registered！");
        }

        String newIdentityId = UUID.randomUUID().toString();

        // 2. 建立身分主體 (先存明文密碼，之後再換 BCrypt)
        Identity newUser = new Identity();
        newUser.setIdentityId(newIdentityId);
        newUser.setUsername(request.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        newUser.setRole("[\"USER\", \"ARCHITECT\"]"); // 賦予雙重身分
        identityRepo.save(newUser);

        // 3. 聯動建立「可用時間」帳戶
        AssetAccount sourceAccount = new AssetAccount();
        sourceAccount.setAccountId(UUID.randomUUID().toString());
        sourceAccount.setIdentityId(newIdentityId);
        sourceAccount.setAccountName("Available Time Pool"); //可用時間池
        sourceAccount.setAccountType("SOURCE");
        sourceAccount.setBalance(BigDecimal.ZERO);
        accountRepo.save(sourceAccount);

        // 4. 聯動建立「充電資產」帳戶
        AssetAccount targetAccount = new AssetAccount();
        targetAccount.setAccountId(UUID.randomUUID().toString());
        targetAccount.setIdentityId(newIdentityId);
        targetAccount.setAccountName("Leisure Asset"); //充電資產 (休閒)
        targetAccount.setAccountType("TARGET");
        targetAccount.setBalance(BigDecimal.ZERO);
        accountRepo.save(targetAccount);

        return newUser;
    }
}