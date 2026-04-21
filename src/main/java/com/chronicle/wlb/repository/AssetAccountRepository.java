package com.chronicle.wlb.repository;

import com.chronicle.wlb.entity.AssetAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetAccountRepository extends JpaRepository<AssetAccount, String> {
    // 🌟 架構師加碼：這行能讓專員把某個 Wojak 的「所有帳戶」一次全部找出來！
    List<AssetAccount> findByIdentityId(String identityId);
}