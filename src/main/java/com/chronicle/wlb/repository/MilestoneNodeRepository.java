package com.chronicle.wlb.repository;

import com.chronicle.wlb.entity.MilestoneNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneNodeRepository extends JpaRepository<MilestoneNode, String> {
    // Returns all milestone nodes belonging to the given identity (user).
    List<MilestoneNode> findByIdentityId(String identityId);
}