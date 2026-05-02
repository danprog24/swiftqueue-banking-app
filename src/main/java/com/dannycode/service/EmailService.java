package com.dannycode.service;

import com.dannycode.model.*;
import com.dannycode.repository.EmailLogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailLogRepo emailLogRepo;

    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Async
    public void sendDepositNotification(User user, BigDecimal amount, BigDecimal balance) {
        String subject = "Deposit Successful";
        String body = String.format(
                "Dear %s,\n\nYour deposit of ₦%.2f was successful.\nNew balance: ₦%.2f\n\nThank you for banking with SwiftQueue.",
                user.getFullname(), amount, balance
        );
        sendAndLog(user, subject, body, EmailType.DEPOSIT);
    }

    @Async
    public void sendWithdrawalNotification(User user, BigDecimal amount, BigDecimal balance) {
        String subject = "Withdrawal Successful";
        String body = String.format(
                "Dear %s,\n\nYour withdrawal of ₦%.2f was successful.\nNew balance: ₦%.2f\n\nThank you for banking with SwiftQueue.",
                user.getFullname(), amount, balance
        );
        sendAndLog(user, subject, body, EmailType.WITHDRAWAL);
    }

    @Async
    public void sendTransferNotification(User user, BigDecimal amount, String receiverAccount, BigDecimal balance) {
        String subject = "Transfer Successful";
        String body = String.format(
                "Dear %s,\n\nYour transfer of ₦%.2f to account %s was successful.\nNew balance: ₦%.2f\n\nThank you for banking with SwiftQueue.",
                user.getFullname(), amount, receiverAccount, balance
        );
        sendAndLog(user, subject, body, EmailType.TRANSFER);
    }

    @Async
    public void sendReceivedTransferNotification(User user, BigDecimal amount, String senderAccount) {
        String subject = "Transfer Received";
        String body = String.format(
                "Dear %s,\n\nYou received ₦%.2f from account %s.\n\nThank you for banking with SwiftQueue.",
                user.getFullname(), amount, senderAccount
        );
        sendAndLog(user, subject, body, EmailType.TRANSFER_RECEIVED);
    }

    @Async
    public void sendPasswordChangedNotification(User user) {
        String subject = "Password Changed";
        String body = String.format(
                "Dear %s,\n\nYour password was changed successfully.\nIf you did not do this, contact support immediately.",
                user.getFullname()
        );
        sendAndLog(user, subject, body, EmailType.PASSWORD_CHANGED);
    }

    @Async
    public void sendAccountLockedNotification(User user) {
        String subject = "Account Locked";
        String body = String.format(
                "Dear %s,\n\nYour account has been locked due to too many failed login attempts.\nContact support to unlock.",
                user.getFullname()
        );
        sendAndLog(user, subject, body, EmailType.ACCOUNT_LOCKED);
    }

    // ── core send + log ──────────────────────────────────────

    private void sendAndLog(User user, String subject, String body, EmailType type) {
        EmailLog log = EmailLog.builder()
                .user(user)
                .subject(subject)
                .recipient(user.getEmail())
                .type(type)
                .status(EmailStatus.PENDING)
                .build();

        try {
            Map<String, Object> payload = Map.of(
                    "personalizations", List.of(Map.of(
                            "to", List.of(Map.of("email", user.getEmail()))
                    )),
                    "from", Map.of("email", fromEmail),
                    "subject", "SwiftQueue - " + subject,
                    "content", List.of(Map.of(
                            "type", "text/plain",
                            "value", body
                    ))
            );

            WebClient.builder()
                    .baseUrl("https://api.sendgrid.com")
                    .defaultHeader("Authorization", "Bearer " + sendgridApiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build()
                    .post()
                    .uri("/v3/mail/send")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.setStatus(EmailStatus.SENT);
        } catch (Exception e) {
            log.setStatus(EmailStatus.FAILED);
            log.setErrorMessage(e.getMessage());
        } finally {
            emailLogRepo.save(log);
        }
    }
}