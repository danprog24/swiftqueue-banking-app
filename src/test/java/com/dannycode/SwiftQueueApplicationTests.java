package com.dannycode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "PAYSTACK_SECRET_KEY=sk_test_dummy",
    "PAYSTACK_BASE_URL=https://api.paystack.co",
    "SENDGRID_API_KEY=SG.dummy",
    "SENDGRID_FROM_EMAIL=test@test.com",
    "SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5445/swiftqueue_db",
    "SPRING_DATASOURCE_USERNAME=dannycode",
    "SPRING_DATASOURCE_PASSWORD=35354467",
    "JWT_SECRET=8fKp2mNqR5vXwZ9cLjYtA4eUhB7dG1nPoWs6iVfE3yMx0QrJkCuDlHbTgSz",
    "REDIS_HOST=localhost",
    "REDIS_PORT=6379"
})
class SwiftQueueApplicationTests {

    @Test
    void contextLoads() {
    }
}