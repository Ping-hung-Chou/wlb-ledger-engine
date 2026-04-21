package com.chronicle.wlb.repository;

import com.chronicle.wlb.entity.Identity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentityRepository extends JpaRepository<Identity, String> {
    // 🌟 架構師加碼：這行能讓專員直接用「使用者名稱」幫你找帳號！
    Optional<Identity> findByUsername(String username);
}