package com.dannycode.dto;

import com.dannycode.model.TransactionStatus;
import com.dannycode.model.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {
    private Long id;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private TransactionType transactionType;
    private TransactionStatus status;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
}