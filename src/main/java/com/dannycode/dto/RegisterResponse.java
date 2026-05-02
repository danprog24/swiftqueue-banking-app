package com.dannycode.dto;

import com.dannycode.model.AccountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class RegisterResponse {
    private String token;
    private String refreshToken;
    private long expiresIn;
    private String email;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}