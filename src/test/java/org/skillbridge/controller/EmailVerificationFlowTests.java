package org.skillbridge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skillbridge.entity.User;
import org.skillbridge.entity.VerificationToken;
import org.skillbridge.enums.VerificationChannel;
import org.skillbridge.repository.UserRepository;
import org.skillbridge.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmailVerificationFlowTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepo;
    @Autowired VerificationTokenRepository tokenRepo;

    private String email;

    @BeforeEach
    void setUp() {
        email = "flow-" + System.nanoTime() + "@example.com";
    }

    private String registerNewUser() throws Exception {
        String body = """
            {"firstName":"Flow","lastName":"Test","email":"%s","password":"TestPass123","phone":"08000000000","roles":["STUDENT"]}
            """.formatted(email);
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        return email;
    }

    @Test
    @Transactional  // keep session open for lazy-load access below
    void registerCreatesToken_userStartsUnverified() throws Exception {
        registerNewUser();

        User u = userRepo.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(u.isEmailVerified()).isFalse();

        // Use the repository method that filters by user id + channel — avoids touching lazy proxy
        List<VerificationToken> tokens =
                tokenRepo.findByUserIdAndChannelAndConsumedAtIsNull(u.getId(), VerificationChannel.EMAIL);

        assertThat(tokens).hasSize(1);
        VerificationToken tok = tokens.get(0);
        assertThat(tok.getTokenHash()).hasSize(64);
        assertThat(tok.getConsumedAt()).isNull();
        assertThat(tok.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void verifyInvalidToken_returns404() throws Exception {
        mvc.perform(get("/api/auth/verify-email").param("token", "totally-invalid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resendForNonexistentEmail_returns200() throws Exception {
        mvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ghost-" + System.nanoTime() + "@example.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void verifyEmailWithoutToken_returns400() throws Exception {
        mvc.perform(get("/api/auth/verify-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendWithoutBody_returns400() throws Exception {
        mvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
