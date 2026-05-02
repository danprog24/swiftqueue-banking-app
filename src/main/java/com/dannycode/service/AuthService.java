package com.dannycode.service;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dannycode.config.CustomUserDetails;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final BankAccRepo bankAccRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(1800L) // 30 minutes in seconds
                .email(user.getEmail())
                .build();
        }

    public RegisterResponse register(RegisterRequest request) {

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .fullname(request.getFullname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.CUSTOMER))
                .active(true)
                .locked(false)
                .build();

        userRepo.save(user);

        BankAcc account = BankAcc.builder()
                .user(user)
                .accountNumber(generateAccountNumber())
                .build();

        bankAccRepo.save(account);

        String accessToken = jwtUtil.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return RegisterResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .build();
    }

   public AuthResponse refreshToken(String token) {
    RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(token);
    User user = refreshToken.getUser();

    String newAccessToken = jwtUtil.generateToken(user.getEmail());
    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

    return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken.getToken())
            .expiresIn(1800L) // 30 minutes in seconds
            .email(user.getEmail())
            .build();
}

    public void logout(String token) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(token);
        refreshTokenService.revokeRefreshToken(refreshToken.getUser());
    }

    private String generateAccountNumber() {
        return String.valueOf((long) (Math.random() * 9_000_000_000L) + 1_000_000_000L);
    }

    public void changePassword(ChangePasswordRequest request) {
    Long userId = getAuthenticatedUserId();
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
        throw new RuntimeException("Current password is incorrect");
    }

    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
        throw new RuntimeException("Passwords do not match");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepo.save(user);

    // Revoke refresh token so user has to login again
    refreshTokenService.revokeRefreshToken(user);
}

private Long getAuthenticatedUserId() {
    CustomUserDetails userDetails = (CustomUserDetails)
            SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
    return userDetails.getUserId();
}

}