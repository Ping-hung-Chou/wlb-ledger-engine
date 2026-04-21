package com.chronicle.wlb.controller;

import com.chronicle.wlb.dto.RegisterRequest;
import com.chronicle.wlb.entity.Identity;
import com.chronicle.wlb.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 規定大門的網址路徑
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register") // 接收前端的 POST 請求
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Identity createdUser = authService.registerUser(request);
            return ResponseEntity.ok("Registered successfully! Your account has been created.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}