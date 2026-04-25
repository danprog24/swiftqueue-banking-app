package com.dannycode.service;

import com.dannycode.config.CustomUserDetails;
import com.dannycode.dto.TransactionRequest;
import com.dannycode.dto.TransactionResponse;
import com.dannycode.dto.TransferRequest;
import com.dannycode.model.*;
import com.dannycode.repository.BankAccRepo;
import com.dannycode.repository.TransactionRepo;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepo transactionRepo;
    private final BankAccRepo bankAccRepo;

    @Transactional
    public TransactionResponse deposit(TransactionRequest request) {

        BankAcc account = getMyAccount();

        account.setBalance(account.getBalance().add(request.getAmount()));

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .build();

        return mapToResponse(transactionRepo.save(transaction));
    }

    @Transactional
    public TransactionResponse withdraw(TransactionRequest request) {

        BankAcc account = getMyAccount();

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .build();

        return mapToResponse(transactionRepo.save(transaction));
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {

        if (request.getReceiverAccountNumber() == null) {
            throw new IllegalArgumentException("Receiver account number is required");
        }

        BankAcc sender = getMyAccount();

        BankAcc receiver = bankAccRepo.findByAccountNumberForUpdate(request.getReceiverAccountNumber())
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            throw new RuntimeException("Cannot transfer to your own account");
        }

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));

        Transaction transaction = Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .transactionType(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .build();

        return mapToResponse(transactionRepo.save(transaction));
    }

    public Page<TransactionResponse> getHistory(int page, int size) {

        BankAcc account = getMyAccount();

        return transactionRepo
                .findBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(
                        account.getId(),
                        account.getId(),
                        PageRequest.of(page, size)
                )
                .map(this::mapToResponse);
    }

   
    private BankAcc getMyAccount() {
        Long userId = getAuthenticatedUserId();

        return bankAccRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No bank account found"));
    }

    private Long getAuthenticatedUserId() {
        CustomUserDetails userDetails =
                (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        return userDetails.getUserId();
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
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
                .build();
    }
}