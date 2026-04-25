package com.dannycode.controller;

import com.dannycode.dto.InitializePaymentResponse;
import com.dannycode.dto.TransactionResponse;
import com.dannycode.dto.WithdrawToAccountRequest;
import com.dannycode.service.PaystackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/paystack")
@RequiredArgsConstructor
public class PaystackController {

    private final PaystackService paystackService;

    @PostMapping("/initialize")
    public ResponseEntity<InitializePaymentResponse> initializePayment(
            @RequestParam BigDecimal amount
    ) {
        return ResponseEntity.ok(paystackService.initializePayment(amount));
    }

    @GetMapping("/verify/{reference}")
    public ResponseEntity<TransactionResponse> verifyPayment(
            @PathVariable String reference
    ) {
        return ResponseEntity.ok(paystackService.verifyAndCredit(reference));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdrawToBank(
            @Valid @RequestBody WithdrawToAccountRequest request
    ) {
        return ResponseEntity.ok(paystackService.withdrawToBank(request));
    }
}