package com.chronicle.wlb.service;

import com.chronicle.wlb.dto.LoginRequest;
import com.chronicle.wlb.dto.RegisterRequest;
import com.chronicle.wlb.entity.AssetAccount;
import com.chronicle.wlb.entity.Identity;
import com.chronicle.wlb.repository.AssetAccountRepository;
import com.chronicle.wlb.repository.IdentityRepository;
import com.chronicle.wlb.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IdentityRepository identityRepo;
    private final AssetAccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

    /**
     * Converts the JSON role string stored in the DB (e.g. ["USER","ARCHITECT"])
     * into a plain List<String> without importing a heavy JSON library.
     * Expected format: a JSON array of quoted strings with no nested objects.
     */
    private List<String> parseRoleJson(String roleJson) {
        if (roleJson == null || roleJson.isBlank()) return List.of();
        // Strip brackets and quotes, then split on commas.
        String stripped = roleJson.replaceAll("[\\[\\]\"\\s]", "");
        return Arrays.asList(stripped.split(","));
    }

    /**
     * Validates credentials and issues a JWT token on success.
     * The token subject is identity_id (UUID) — not username —
     * so all downstream services can use it directly as a database FK.
     *
     * @param request login payload containing username and password
     * @return signed JWT token string
     * @throws IllegalArgumentException if the username is not found or the password is wrong
     */
    public String login(LoginRequest request) {
        Identity user = identityRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        // Parse the role JSON string (e.g. ["USER","ARCHITECT"]) into a plain List<String>.
        // This is a lightweight parse to avoid pulling in a full ObjectMapper dependency here.
        List<String> roles = parseRoleJson(user.getRole());

        // Issue a token: subject = identity_id, roles embedded as a JWT claim.
        return jwtUtil.generateToken(user.getIdentityId(), roles);
    }
}