package com.dannycode.repository;

import com.dannycode.model.BankAcc;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BankAccRepo extends JpaRepository<BankAcc, Long> {

    Optional<BankAcc> findByUserId(Long userId);

    Optional<BankAcc> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankAcc b WHERE b.accountNumber = :accountNumber")
    Optional<BankAcc> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}