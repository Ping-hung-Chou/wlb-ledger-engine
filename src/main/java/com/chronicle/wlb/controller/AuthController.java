package com.chronicle.wlb.controller;

import com.chronicle.wlb.dto.LoginRequest;
import com.chronicle.wlb.dto.RegisterRequest;
import com.chronicle.wlb.entity.Identity;
import com.chronicle.wlb.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 規定大門的網址路徑
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Identity createdUser = authService.registerUser(request);
            return ResponseEntity.ok("Registered successfully! Your account has been created.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Authenticates the user and returns a signed JWT token.
     * The token encodes identity_id as its subject — pass it as:
     *   Authorization: Bearer <token>
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}