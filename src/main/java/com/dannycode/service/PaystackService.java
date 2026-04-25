package com.dannycode.service;

import com.dannycode.config.CustomUserDetails;
import com.dannycode.dto.*;
import com.dannycode.model.BankAcc;
import com.dannycode.model.Transaction;
import com.dannycode.model.TransactionStatus;
import com.dannycode.model.TransactionType;
import com.dannycode.repository.BankAccRepo;
import com.dannycode.repository.TransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaystackService {

    private final WebClient paystackWebClient;
    private final BankAccRepo bankAccRepo;
    private final TransactionRepo transactionRepo;

    // ── Step 1: Initialize payment ──

    public InitializePaymentResponse initializePayment(BigDecimal amount) {
        BankAcc account = getMyAccount();

        String reference = "SQ-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase();

        Map<String, Object> body = Map.of(
                "email", account.getUser().getEmail(),
                "amount", amount.multiply(BigDecimal.valueOf(100)).longValue(),
                "reference", reference
        );

        return paystackWebClient.post()
                .uri("/transaction/initialize")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(InitializePaymentResponse.class)
                .block();
    }

    // ── Step 2: Verify payment and credit account ──

    @Transactional
    public TransactionResponse verifyAndCredit(String reference) {
        BankAcc account = getMyAccount();

        VerifyPaymentResponse response = paystackWebClient.get()
                .uri("/transaction/verify/" + reference)
                .retrieve()
                .bodyToMono(VerifyPaymentResponse.class)
                .block();

        if (response == null || !response.isStatus()) {
            throw new RuntimeException("Payment verification failed");
        }

        if (!"success".equals(response.getData().getStatus())) {
            throw new RuntimeException("Payment was not successful");
        }

        BigDecimal amount = BigDecimal.valueOf(response.getData().getAmount())
                .divide(BigDecimal.valueOf(100));

        account.setBalance(account.getBalance().add(amount));
        bankAccRepo.save(account);

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .description("Paystack funding - ref: " + reference)
                .build();

        return mapToResponse(transactionRepo.save(transaction));
    }

    // ── Step 3: Withdraw to real bank account ──
    @Transactional
    @SuppressWarnings("unchecked")
    public TransactionResponse withdrawToBank(WithdrawToAccountRequest request) {
        BankAcc account = getMyAccount();

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Step 3a: Create transfer recipient
        Map<String, Object> recipientBody = Map.of(
                "type", "nuban",
                "name", request.getAccountName(),
                "account_number", request.getAccountNumber(),
                "bank_code", request.getBankCode(),
                "currency", "NGN"
        );

        Map<String, Object> recipientResponse = paystackWebClient.post()
                .uri("/transferrecipient")
                .bodyValue(recipientBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        Map<String, Object> recipientData = (Map<String, Object>) recipientResponse.get("data");
        String recipientCode = (String) recipientData.get("recipient_code");

        // Step 3b: Initiate transfer
        String reference = "SQ-WD-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10).toUpperCase();

        Map<String, Object> transferBody = Map.of(
                "source", "balance",
                "amount", request.getAmount().multiply(BigDecimal.valueOf(100)).longValue(),
                "recipient", recipientCode,
                "reason", "SwiftQueue withdrawal",
                "reference", reference
        );

        paystackWebClient.post()
                .uri("/transfer")
                .bodyValue(transferBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        // Debit user balance
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        bankAccRepo.save(account);

        Transaction transaction = Transaction.builder()
                .senderAccount(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .amount(request.getAmount())
                .description("Bank withdrawal to " + request.getAccountNumber())
                .build();

        return mapToResponse(transactionRepo.save(transaction));
    }

    // ── helpers ──────────────────────────────────────────────

    private BankAcc getMyAccount() {
        CustomUserDetails userDetails = (CustomUserDetails)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();
        Long userId = userDetails.getUserId();
        return bankAccRepo.findByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No bank account found"));
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