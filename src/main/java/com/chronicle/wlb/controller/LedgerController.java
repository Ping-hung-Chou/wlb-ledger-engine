package com.chronicle.wlb.controller;

import com.chronicle.wlb.dto.CheckoutRequest;
import com.chronicle.wlb.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping("/checkout") // 接收前端的 POST 請求
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        try {
            ledgerService.processCheckout(request);
            return ResponseEntity.ok("Success! Time exchanged into assets, and record in time_ledgers");
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 捕捉樂觀鎖或餘額不足的錯誤，回傳 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}