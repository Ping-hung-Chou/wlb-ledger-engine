package com.chronicle.wlb.service;

import com.chronicle.wlb.dto.CreateMilestoneRequest;
import com.chronicle.wlb.dto.MilestoneResponse;
import com.chronicle.wlb.entity.MilestoneNode;
import com.chronicle.wlb.repository.MilestoneNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for milestone lifecycle management.
 * The caller is responsible for supplying the authenticated identity ID —
 * this service never reads identity context from the request payload.
 */
@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneNodeRepository milestoneRepo;

    /**
     * Creates a new MilestoneNode and persists it to the database.
     * Fields managed automatically by @PrePersist (not set here):
     *   nodeId, createdAt, actualMinutes, timeMultiplier, status.
     *
     * @param req              validated request payload from the controller
     * @param callerIdentityId identity ID of the authenticated user (injected by controller, not from req)
     * @return a fully populated MilestoneResponse reflecting the saved entity
     */
    @Transactional(rollbackFor = Exception.class)
    public MilestoneResponse create(CreateMilestoneRequest req, String callerIdentityId) {

        MilestoneNode node = new MilestoneNode();
        node.setIdentityId(callerIdentityId);
        node.setTitle(req.getTitle());
        node.setDescription(req.getDescription());
        node.setMilestoneType(req.getMilestoneType());
        node.setTargetMinutes(req.getTargetMinutes());
        // status, actualMinutes, timeMultiplier, createdAt, nodeId
        // are all initialized automatically by @PrePersist — do not set them here.

        MilestoneNode savedNode = milestoneRepo.save(node);

        return mapToResponse(savedNode);
    }

    /**
     * Maps every field from a MilestoneNode entity to a MilestoneResponse DTO.
     * Keeps the controller layer free of entity references.
     */
    private MilestoneResponse mapToResponse(MilestoneNode node) {
        MilestoneResponse res = new MilestoneResponse();
        res.setNodeId(node.getNodeId());
        res.setIdentityId(node.getIdentityId());
        res.setTitle(node.getTitle());
        res.setDescription(node.getDescription());
        res.setMilestoneType(node.getMilestoneType());
        res.setStatus(node.getStatus());
        res.setTargetMinutes(node.getTargetMinutes());
        res.setActualMinutes(node.getActualMinutes());
        res.setTimeMultiplier(node.getTimeMultiplier());
        res.setCreatedAt(node.getCreatedAt());
        res.setStartedAt(node.getStartedAt());
        res.setCompletedAt(node.getCompletedAt());
        res.setPausedAt(node.getPausedAt());
        return res;
    }
}
