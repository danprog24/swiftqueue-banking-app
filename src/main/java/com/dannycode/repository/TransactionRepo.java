package com.dannycode.repository;

import com.dannycode.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {

    Page<Transaction> findBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(
            Long senderId,
            Long receiverId,
            Pageable pageable
    );
}