package org.skillbridge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skillbridge.entity.User;
import org.skillbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginRequiresVerificationTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepo;
    @Autowired PasswordEncoder encoder;

    private String registerUnverifiedUser() throws Exception {
        String email = "gate-" + System.nanoTime() + "@example.com";
        String body = """
            {"firstName":"Gate","lastName":"Test","email":"%s","password":"TestPass123","phone":"08000000000","roles":["STUDENT"]}
            """.formatted(email);
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        return email;
    }

    private String registerVerifiedUser() throws Exception {
        String email = registerUnverifiedUser();
        User u = userRepo.findByEmailIgnoreCase(email).orElseThrow();
        u.setEmailVerified(true);
        userRepo.save(u);
        return email;
    }

    @Test
    void unverifiedUser_loginReturns403EmailNotVerified() throws Exception {
        String email = registerUnverifiedUser();
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"TestPass123\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Email Not Verified"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Email verification required")));
    }

    @Test
    void verifiedUser_loginSucceeds() throws Exception {
        String email = registerVerifiedUser();
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"TestPass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.emailVerified").value(true));
    }

    @Test
    void registrationResponse_includesEmailVerifiedFalse() throws Exception {
        String email = "resp-" + System.nanoTime() + "@example.com";
        String body = """
            {"firstName":"R","lastName":"T","email":"%s","password":"TestPass123","phone":"08000000000","roles":["STUDENT"]}
            """.formatted(email);
        var res = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = om.readTree(res.getResponse().getContentAsString());
        assertThat(node.get("user").get("emailVerified").asBoolean()).isFalse();
    }
}
