package com.dannycode.service;

import com.dannycode.dto.AccResponse;
import com.dannycode.dto.TransactionResponse;
import com.dannycode.model.BankAcc;
import com.dannycode.model.User;
import com.dannycode.repository.BankAccRepo;
import com.dannycode.repository.TransactionRepo;
import com.dannycode.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepo userRepo;
    private final BankAccRepo bankAccRepo;
    private final TransactionRepo transactionRepo;

    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepo.findAll(pageable);
    }

    public void lockUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLocked(true);
        userRepo.save(user);
    }

    public void unlockUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        userRepo.save(user);
    }

    public Page<TransactionResponse> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return transactionRepo.findAll(pageable).map(transaction ->
                TransactionResponse.builder()
                        .id(transaction.getId())
                        .senderAccountNumber(transaction.getSenderAccount().getAccountNumber())
                        .receiverAccountNumber(
                                transaction.getReceiverAccount() != null
                                        ? transaction.getReceiverAccount().getAccountNumber()
                                        : null
                        )
                        .transactionType(transaction.getTransactionType())
                        .status(transaction.getStatus())
                        .amount(transaction.getAmount())
                        .description(transaction.getDescription())
                        .createdAt(transaction.getCreatedAt())
                        .build()
        );
    }

    public AccResponse getUserAccount(Long userId) {
        BankAcc account = bankAccRepo.findByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No bank account found"));
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