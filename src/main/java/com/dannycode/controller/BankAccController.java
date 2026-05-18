package com.dannycode.controller;

import com.dannycode.dto.AccResponse;
import com.dannycode.dto.UpgradeAccRequest;
import com.dannycode.model.User;
import com.dannycode.repository.BankAccRepo;
import com.dannycode.repository.UserRepo;
import com.dannycode.service.BankAccService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class BankAccController {

    private final BankAccService bankAccountService;
    private final UserRepo userRepo;
    private final BankAccRepo accountRepo;


    @GetMapping("/my")
    public ResponseEntity<List<AccResponse>> getMyAccounts(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AccResponse> response = accountRepo.findByUserId(user.getId())
                .stream()
                .map(account -> AccResponse.builder()
                        .id(account.getId())
                        .fullName(account.getUser().getFullName())
                        .email(account.getUser().getEmail())
                        .accountNumber(account.getAccountNumber())
                        .accountType(account.getAccountType())
                        .balance(account.getBalance())
                        .active(account.isActive())
                        .createdAt(account.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
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