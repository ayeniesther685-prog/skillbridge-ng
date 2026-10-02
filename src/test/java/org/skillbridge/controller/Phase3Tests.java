package org.skillbridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skillbridge.dto.request.RegisterRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Phase3Tests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepository;

    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest reg = new RegisterRequest(
                "Test", "User", email, "Password123", null, Set.of(RoleName.FREELANCER));
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("accessToken").asText();
    }

    @Test
    void fullProfileFlow() throws Exception {
        String email = "p3-" + UUID.randomUUID() + "@example.com";
        String token = registerAndGetToken(email);

        // 1. Upsert profile
        mockMvc.perform(put("/api/users/me/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"headline":"Backend Developer","website":"https://example.com",
                                 "githubUrl":"https://github.com/test","availability":"OPEN",
                                 "hourlyRate":25.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Backend Developer"))
                .andExpect(jsonPath("$.hourlyRate").value(25.00));

        // 2. Get profile
        mockMvc.perform(get("/api/users/me/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Backend Developer"));

        // 3. Add education
        mockMvc.perform(post("/api/users/me/education")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"institution":"University of Lagos","degree":"BSc",
                                 "fieldOfStudy":"Computer Science","startDate":"2018-09-01","endDate":"2022-06-30"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.institution").value("University of Lagos"));

        // 4. Add work experience
        mockMvc.perform(post("/api/users/me/experience")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"Paystack","position":"Backend Engineer","current":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company").value("Paystack"))
                .andExpect(jsonPath("$.current").value(true));

        // 5. Add certification
        mockMvc.perform(post("/api/users/me/certifications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"AWS Cloud Practitioner","issuingOrganization":"Amazon"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("AWS Cloud Practitioner"));

        // 6. Add portfolio item
        mockMvc.perform(post("/api/users/me/portfolio")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Payment API","technologies":"Java, Spring Boot"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Payment API"));

        // 7. Link a skill (Java, id=1 seeded by V5)
        mockMvc.perform(post("/api/users/me/skills/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Java")));
    }

    @Test
    void skillsArePubliclyListable() throws Exception {
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[?(@.name=='Java')]", not(empty())));
    }

    @Test
    void profileRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/users/me/profile"))
                .andExpect(status().isUnauthorized());
    }
}