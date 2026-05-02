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
    "SENDGRID_FROM_EMAIL=test@test.com"
})
class SwiftQueueApplicationTests {

    @Test
    void contextLoads() {
    }
}