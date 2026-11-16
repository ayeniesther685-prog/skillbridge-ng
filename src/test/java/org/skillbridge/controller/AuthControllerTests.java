package org.skillbridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skillbridge.dto.request.LoginRequest;
import org.skillbridge.dto.request.RegisterRequest;
import org.skillbridge.entity.User;
import org.skillbridge.enums.RoleName;
import org.skillbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepository;

    /**
     * Phase 3A: new registrations start email-unverified. The register->login->me flow
     * is preserved by marking the test user verified between register and login, matching
     * the behavior for pre-verification users who are already on the platform.
     */
    private void markVerified(String email) {
        User u = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        u.setEmailVerified(true);
        userRepository.save(u);
    }

    @Test
    void register_login_andMe_flow() throws Exception {
        String email = "it-" + UUID.randomUUID() + "@example.com";
        RegisterRequest reg = new RegisterRequest(
                "Test", "User", email, "Password123", null, Set.of(RoleName.STUDENT));

        // Register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.emailVerified").value(false));

        assertThat(userRepository.findByEmailIgnoreCase(email)).isPresent();

        // Phase 3A: mark verified so login proceeds (otherwise login returns 403)
        markVerified(email);

        // Login
        LoginRequest login = new LoginRequest(email, "Password123");
        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.emailVerified").value(true))
                .andReturn().getResponse().getContentAsString();

        String accessToken = om.readTree(loginJson).get("accessToken").asText();

        // Me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.roles[0]").value("STUDENT"));
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        String email = "it-" + UUID.randomUUID() + "@example.com";
        RegisterRequest reg = new RegisterRequest(
                "Test", "User", email, "Password123", null, Set.of(RoleName.STUDENT));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest bad = new LoginRequest(email, "WrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unverified_returns403() throws Exception {
        // New test for Phase 3A behavior — verifies gate without marking user verified.
        String email = "unverified-" + UUID.randomUUID() + "@example.com";
        RegisterRequest reg = new RegisterRequest(
                "Test", "User", email, "Password123", null, Set.of(RoleName.STUDENT));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest(email, "Password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(login)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Email Not Verified"));
    }
}
