package com.chronicle.wlb.controller;

import com.chronicle.wlb.dto.CreateMilestoneRequest;
import com.chronicle.wlb.dto.MilestoneResponse;
import com.chronicle.wlb.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for milestone goal management.
 * All endpoints require a valid JWT — the identity_id is extracted from the token
 * via authentication.getName() and passed to the service layer as the owner FK.
 */
@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    /**
     * Creates a new milestone node owned by the authenticated user.
     * Restricted to users with the ARCHITECT role — regular USER accounts are blocked.
     *
     * @param req            validated request payload
     * @param authentication injected by Spring Security; principal name = identity_id (UUID)
     * @return 201 Created with the persisted milestone details
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ARCHITECT')")
    public ResponseEntity<MilestoneResponse> create(
            @RequestBody @Valid CreateMilestoneRequest req,
            Authentication authentication) {

        // Retrieve the caller's identity_id from the JWT principal — never from the request body.
        String callerIdentityId = authentication.getName();

        MilestoneResponse response = milestoneService.create(req, callerIdentityId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
