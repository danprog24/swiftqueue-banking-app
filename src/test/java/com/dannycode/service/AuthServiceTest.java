package com.dannycode.service;

import com.dannycode.config.JwtUtil;
import com.dannycode.dto.AuthResponse;
import com.dannycode.dto.ChangePasswordRequest;
import com.dannycode.dto.LoginRequest;
import com.dannycode.dto.RegisterRequest;
import com.dannycode.dto.RegisterResponse;
import com.dannycode.model.BankAcc;
import com.dannycode.model.RefreshToken;
import com.dannycode.model.Role;
import com.dannycode.model.User;
import com.dannycode.repository.BankAccRepo;
import com.dannycode.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepo userRepo;
    @Mock private BankAccRepo bankAccRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private BankAcc testAccount;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullname("John Doe")
                .email("john@example.com")
                .phone("08012345678")
                .password("encodedPassword")
                .roles(Set.of(Role.CUSTOMER))
                .active(true)
                .locked(false)
                .failedLoginAttempts(0)
                .build();

        testAccount = BankAcc.builder()
                .id(1L)
                .accountNumber("1234567890")
                .user(testUser)
                .balance(BigDecimal.ZERO)
                .build();

        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-uuid")
                .user(testUser)
                .expiresIn(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
    }

    // ── Register ──────────────────────────────────────────────

    @Test
    void register_ShouldReturnRegisterResponse_WhenValidRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFullname("John Doe");
        request.setEmail("john@example.com");
        request.setPhone("08012345678");
        request.setPassword("password123");

        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(testUser);
        when(bankAccRepo.save(any(BankAcc.class))).thenReturn(testAccount);
        when(jwtUtil.generateToken(anyString())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(testRefreshToken);

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token-uuid", response.getRefreshToken());
        assertEquals("john@example.com", response.getEmail());

        verify(userRepo).existsByEmail("john@example.com");
        verify(userRepo).save(any(User.class));
        verify(bankAccRepo).save(any(BankAcc.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@example.com");

        when(userRepo.existsByEmail(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    // ── Login ─────────────────────────────────────────────────

    @Test
    void login_ShouldReturnAuthResponse_WhenValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyString())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(testRefreshToken);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token-uuid", response.getRefreshToken());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(1800L, response.getExpiresIn());

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password123");

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );

        assertEquals("User not found", exception.getMessage());
    }

    // ── Refresh Token ─────────────────────────────────────────

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenValidRefreshToken() {
        when(refreshTokenService.verifyRefreshToken(anyString())).thenReturn(testRefreshToken);
        when(jwtUtil.generateToken(anyString())).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(testRefreshToken);

        AuthResponse response = authService.refreshToken("refresh-token-uuid");

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(1800L, response.getExpiresIn());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenInvalidRefreshToken() {
        when(refreshTokenService.verifyRefreshToken(anyString()))
                .thenThrow(new RuntimeException("Refresh token not found"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken("invalid-token")
        );

        assertEquals("Refresh token not found", exception.getMessage());
    }

    // ── Logout ────────────────────────────────────────────────

    @Test
    void logout_ShouldRevokeRefreshToken_WhenValidToken() {
        when(refreshTokenService.verifyRefreshToken(anyString())).thenReturn(testRefreshToken);

        authService.logout("refresh-token-uuid");

        verify(refreshTokenService).revokeRefreshToken(testUser);
    }

    // ── Change Password ───────────────────────────────────────

    @Test
    void changePassword_ShouldUpdatePassword_WhenValidRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");

        // Mock security context for getAuthenticatedUserId
        org.springframework.security.core.context.SecurityContext securityContext =
                mock(org.springframework.security.core.context.SecurityContext.class);
        org.springframework.security.core.Authentication authentication =
                mock(org.springframework.security.core.Authentication.class);
        com.dannycode.config.CustomUserDetails userDetails =
                mock(com.dannycode.config.CustomUserDetails.class);

        org.springframework.security.core.context.SecurityContextHolder
                .setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        authService.changePassword(request);

        verify(userRepo).save(testUser);
        verify(refreshTokenService).revokeRefreshToken(testUser);
        assertEquals("newEncodedPassword", testUser.getPassword());
    }

    @Test
    void changePassword_ShouldThrowException_WhenCurrentPasswordWrong() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");

        org.springframework.security.core.context.SecurityContext securityContext =
                mock(org.springframework.security.core.context.SecurityContext.class);
        org.springframework.security.core.Authentication authentication =
                mock(org.springframework.security.core.Authentication.class);
        com.dannycode.config.CustomUserDetails userDetails =
                mock(com.dannycode.config.CustomUserDetails.class);

        org.springframework.security.core.context.SecurityContextHolder
                .setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.changePassword(request)
        );

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepo, never()).save(any());
    }

    @Test
    void changePassword_ShouldThrowException_WhenPasswordsDoNotMatch() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("differentPassword");

        org.springframework.security.core.context.SecurityContext securityContext =
                mock(org.springframework.security.core.context.SecurityContext.class);
        org.springframework.security.core.Authentication authentication =
                mock(org.springframework.security.core.Authentication.class);
        com.dannycode.config.CustomUserDetails userDetails =
                mock(com.dannycode.config.CustomUserDetails.class);

        org.springframework.security.core.context.SecurityContextHolder
                .setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.changePassword(request)
        );

        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepo, never()).save(any());
    }
}