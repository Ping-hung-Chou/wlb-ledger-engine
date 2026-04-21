package com.chronicle.wlb.repository;

import com.chronicle.wlb.entity.AiWeeklyInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiWeeklyInsightRepository extends JpaRepository<AiWeeklyInsight, String> {
    // 用身分 ID 和 年份週數（例如 2026-W11）來找當週的信件
    Optional<AiWeeklyInsight> findByIdentityIdAndYearWeek(String identityId, String yearWeek);
}