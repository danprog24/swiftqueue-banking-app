package com.dannycode.service;

import com.dannycode.config.CustomUserDetails;
import com.dannycode.dto.AccResponse;
import com.dannycode.dto.UpgradeAccRequest;
import com.dannycode.model.*;
import com.dannycode.repository.BankAccRepo;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccServiceTest {

    @Mock private BankAccRepo bankAccRepo;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;
    @Mock private CustomUserDetails customUserDetails;

    @InjectMocks
    private BankAccService bankAccService;

    private User testUser;
    private BankAcc testAccount;

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
                .accountType(AccountType.SAVINGS)
                .active(true)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserId()).thenReturn(1L);
        when(bankAccRepo.findByUserId(1L)).thenReturn(Optional.of(testAccount));
    }

    @Test
    void getBalance_ShouldReturnAccountDetails_WhenAccountExists() {
        AccResponse response = bankAccService.getBalance();

        assertNotNull(response);
        assertEquals("1234567890", response.getAccountNumber());
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
        assertEquals(AccountType.SAVINGS, response.getAccountType());
        assertTrue(response.isActive());
    }

    @Test
    void getMyAccounts_ShouldReturnListOfAccounts() {
        List<AccResponse> accounts = bankAccService.getMyAccounts();

        assertNotNull(accounts);
        assertEquals(1, accounts.size());
        assertEquals("1234567890", accounts.get(0).getAccountNumber());
    }

    @Test
    void upgradeAccount_ShouldChangeAccountType_WhenValidRequest() {
        UpgradeAccRequest request = new UpgradeAccRequest();
        request.setAccountType(AccountType.CURRENT);

        when(bankAccRepo.save(any(BankAcc.class))).thenReturn(testAccount);

        AccResponse response = bankAccService.upgradeAccount(request);

        assertEquals(AccountType.CURRENT, testAccount.getAccountType());
        verify(bankAccRepo).save(testAccount);
    }

    @Test
    void getBalance_ShouldThrowException_WhenNoAccountFound() {
        when(bankAccRepo.findByUserId(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bankAccService.getBalance()
        );

        assertEquals("No bank account found", exception.getMessage());
    }
}