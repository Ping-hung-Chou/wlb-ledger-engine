package com.chronicle.wlb.repository;

import com.chronicle.wlb.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
    // 系統全域參數通常只要基本的 CRUD 就夠用了，不用額外寫方法！
}