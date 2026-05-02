package com.dannycode.repository;

import com.dannycode.model.EmailLog;
import com.dannycode.model.EmailStatus;
import com.dannycode.model.EmailType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailLogRepo extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findByUserId(Long userId);
    List<EmailLog> findByStatus(EmailStatus status);
    List<EmailLog> findByType(EmailType type);
}