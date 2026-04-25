package com.dannycode.service;

import com.dannycode.config.CustomUserDetails;
import com.dannycode.dto.AccResponse;
import com.dannycode.dto.UpgradeAccRequest;
import com.dannycode.model.BankAcc;
import com.dannycode.repository.BankAccRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccService {

    private final BankAccRepo bankAccRepo;

    public List<AccResponse> getMyAccounts() {
        Long userId = getAuthenticatedUserId();
        return bankAccRepo.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AccResponse upgradeAccount(UpgradeAccRequest request) {
        Long userId = getAuthenticatedUserId();

        BankAcc account = bankAccRepo.findByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No bank account found"));

        account.setAccountType(request.getAccountType());
        bankAccRepo.save(account);
        return mapToResponse(account);
    }

    // ── helpers ──────────────────────────────────────────────

    private Long getAuthenticatedUserId() {
        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();
        return userDetails.getUserId();
    }

    private AccResponse mapToResponse(BankAcc account) {
        return AccResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .active(account.isActive())
                .createdAt(account.getCreatedAt())
                .build();
    }
}