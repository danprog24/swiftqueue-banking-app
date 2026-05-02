package com.dannycode.controller;

import com.dannycode.dto.AccResponse;
import com.dannycode.dto.TransactionResponse;
import com.dannycode.model.User;
import com.dannycode.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    @PatchMapping("/users/{id}/lock")
    public ResponseEntity<String> lockUser(@PathVariable Long id) {
        adminService.lockUser(id);
        return ResponseEntity.ok("User locked successfully");
    }

    @PatchMapping("/users/{id}/unlock")
    public ResponseEntity<String> unlockUser(@PathVariable Long id) {
        adminService.unlockUser(id);
        return ResponseEntity.ok("User unlocked successfully");
    }

    @GetMapping("/users/{id}/account")
    public ResponseEntity<AccResponse> getUserAccount(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserAccount(id));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getAllTransactions(page, size));
    }
}