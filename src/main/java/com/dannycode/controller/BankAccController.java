package com.dannycode.controller;

import com.dannycode.dto.AccResponse;
import com.dannycode.dto.UpgradeAccRequest;
import com.dannycode.service.BankAccService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class BankAccController {

    private final BankAccService bankAccountService;

    @GetMapping("/my")
    public ResponseEntity<List<AccResponse>> getMyAccounts() {
        return ResponseEntity.ok(bankAccountService.getMyAccounts());
    }

    @PatchMapping("/upgrade")
    public ResponseEntity<AccResponse> upgradeAccount(
            @RequestBody UpgradeAccRequest request
    ) {
        return ResponseEntity.ok(bankAccountService.upgradeAccount(request));
    }
}