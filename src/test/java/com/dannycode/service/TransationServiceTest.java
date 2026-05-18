package com.dannycode.service;

import com.dannycode.config.CustomUserDetails;
import com.dannycode.dto.TransactionRequest;
import com.dannycode.dto.TransactionResponse;
import com.dannycode.dto.TransferRequest;
import com.dannycode.model.*;
import com.dannycode.repository.BankAccRepo;
import com.dannycode.repository.TransactionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepo transactionRepo;
    @Mock private BankAccRepo bankAccRepo;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;
    @Mock private CustomUserDetails customUserDetails;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private BankAcc testAccount;
    private BankAcc receiverAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("08012345678")
                .password("encodedPassword")
                .roles(Set.of(Role.CUSTOMER))
                .active(true)
                .locked(false)
                .build();

        testAccount = BankAcc.builder()
                .id(1L)
                .accountNumber("1234567890")
                .user(testUser)
                .balance(new BigDecimal("5000.00"))
                .build();

        receiverAccount = BankAcc.builder()
                .id(2L)
                .accountNumber("0987654321")
                .user(User.builder().id(2L).email("jane@example.com").build())
                .balance(new BigDecimal("1000.00"))
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(1L);
        when(bankAccRepo.findByUserId(1L)).thenReturn(Optional.of(testAccount));
    }

    // ── Deposit ───────────────────────────────────────────────

    @Test
    void deposit_ShouldIncreaseBalance_WhenValidRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("1000.00"));
        request.setDescription("Test deposit");

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .senderAccount(testAccount)
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(TransactionStatus.SUCCESS)
                .build();

        when(transactionRepo.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.deposit(request);

        assertNotNull(response);
        assertEquals(TransactionType.DEPOSIT, response.getTransactionType());
        assertEquals(new BigDecimal("1000.00"), response.getAmount());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("6000.00"), testAccount.getBalance());

        verify(bankAccRepo).save(testAccount);
        verify(transactionRepo).save(any(Transaction.class));
    }

    // ── Withdraw ──────────────────────────────────────────────

    @Test
    void withdraw_ShouldDecreaseBalance_WhenSufficientFunds() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("1000.00"));
        request.setDescription("Test withdrawal");

        Transaction savedTransaction = Transaction.builder()
                .id(1L)
                .senderAccount(testAccount)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .status(TransactionStatus.SUCCESS)
                .build();

        when(transactionRepo.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.withdraw(request);

        assertNotNull(response);
        assertEquals(TransactionType.WITHDRAWAL, response.getTransactionType());
        assertEquals(new BigDecimal("4000.00"), testAccount.getBalance());
        verify(bankAccRepo).save(testAccount);
    }

    @Test
    void withdraw_ShouldThrowException_WhenInsufficientFunds() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("10000.00"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> transactionService.withdraw(request)
        );

        assertEquals("Insufficient balance", exception.getMessage());
        verify(bankAccRepo, never()).save(any());
        verify(transactionRepo, never()).save(any());
    }

    // ── Transfer ──────────────────────────────────────────────

   @Test
void transfer_ShouldMoveFunds_WhenValidRequest() {
    TransferRequest request = new TransferRequest();
    request.setReceiverAccountNumber("0987654321");
    request.setAmount(new BigDecimal("1000.00"));
    request.setDescription("Test transfer");

    when(bankAccRepo.findByAccountNumberForUpdate("0987654321"))
            .thenReturn(Optional.of(receiverAccount));

    Transaction savedTransaction = Transaction.builder()
            .id(1L)
            .senderAccount(testAccount)
            .receiverAccount(receiverAccount)
            .transactionType(TransactionType.TRANSFER)
            .amount(request.getAmount())
            .status(TransactionStatus.SUCCESS)
            .build();

    when(transactionRepo.save(any(Transaction.class))).thenReturn(savedTransaction);

    TransactionResponse response = transactionService.transfer(request);

    assertNotNull(response);
    assertEquals(TransactionType.TRANSFER, response.getTransactionType());
    assertEquals(new BigDecimal("4000.00"), testAccount.getBalance());
    assertEquals(new BigDecimal("2000.00"), receiverAccount.getBalance());
    verify(bankAccRepo).save(testAccount);
    verify(bankAccRepo).save(receiverAccount);
}

@Test
void transfer_ShouldThrowException_WhenTransferringToOwnAccount() {
    TransferRequest request = new TransferRequest();
    request.setReceiverAccountNumber("1234567890");
    request.setAmount(new BigDecimal("1000.00"));

    when(bankAccRepo.findByAccountNumberForUpdate("1234567890"))
            .thenReturn(Optional.of(testAccount));

    RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> transactionService.transfer(request)
    );

    assertEquals("Cannot transfer to your own account", exception.getMessage());
}

@Test
void transfer_ShouldThrowException_WhenInsufficientFunds() {
    TransferRequest request = new TransferRequest();
    request.setReceiverAccountNumber("0987654321");
    request.setAmount(new BigDecimal("10000.00"));

    when(bankAccRepo.findByAccountNumberForUpdate("0987654321"))
            .thenReturn(Optional.of(receiverAccount));

    RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> transactionService.transfer(request)
    );

    assertEquals("Insufficient balance", exception.getMessage());
}

@Test
void transfer_ShouldThrowException_WhenReceiverNotFound() {
    TransferRequest request = new TransferRequest();
    request.setReceiverAccountNumber("9999999999");
    request.setAmount(new BigDecimal("1000.00"));

    // No stubbing needed - default returns empty Optional
    RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> transactionService.transfer(request)
    );

    assertEquals("Receiver account not found", exception.getMessage());
   }
}