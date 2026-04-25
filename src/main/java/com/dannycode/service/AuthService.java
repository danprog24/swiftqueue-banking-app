package com.dannycode.service;

import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dannycode.config.JwtUtil;
import com.dannycode.dto.JwtResponse;
import com.dannycode.dto.LoginRequest;
import com.dannycode.dto.RegisterRequest;
import com.dannycode.dto.RegisterResponse;
import com.dannycode.model.BankAcc;
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

    public JwtResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtUtil.generateToken(request.getEmail());
        return new JwtResponse(token, request.getEmail());
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

        String token = jwtUtil.generateToken(user.getEmail());

        return RegisterResponse.builder()
                .token(token)
                .email(user.getEmail())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .build();
    }
    // ── helpers ──────────────────────────────────────────────

    private String generateAccountNumber() {
        return String.valueOf((long) (Math.random() * 9_000_000_000L) + 1_000_000_000L);
    }

}
