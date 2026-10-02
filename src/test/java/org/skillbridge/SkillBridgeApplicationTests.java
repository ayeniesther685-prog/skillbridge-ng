package org.skillbridge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SkillBridgeApplicationTests {

    @Test
    void contextLoads() {
        // Fails fast if Flyway, JPA, or the Spring context is misconfigured.
    }
}