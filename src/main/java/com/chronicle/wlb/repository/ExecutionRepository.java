package com.chronicle.wlb.repository;

import com.chronicle.wlb.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, String> {
    // 透過活動名稱來尋找倍率（例如找「畫漫畫」的資料）
    Optional<Execution> findByActivityName(String activityName);
}