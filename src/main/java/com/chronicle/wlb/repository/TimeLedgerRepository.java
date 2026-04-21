package com.chronicle.wlb.repository;

import com.chronicle.wlb.entity.TimeLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeLedgerRepository extends JpaRepository<TimeLedger, String> {

    // 🌟 只要依照命名規則寫下這行，Spring 會自動幫你生出撈取特定使用者所有紀錄的 SQL 語句！
    List<TimeLedger> findByIdentityId(String identityId);

}