package org.skillbridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skillbridge.dto.request.RegisterRequest;
import org.skillbridge.enums.RoleName;
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
class Phase5Tests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    private String reg(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        RegisterRequest r = new RegisterRequest("T", "U", email, "Password123", null, Set.of(RoleName.FREELANCER));
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(r)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("accessToken").asText();
    }

    @Test
    void messagingFlow() throws Exception {
        String tA = reg("msgA");
        String tB = reg("msgB");

        // A looks up B via /api/users
              // Get user B's id from their own /me endpoint — simpler and doesn't
        // depend on /api/users response shape or email case normalization.
        String meBJson = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + tB))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long userBId = om.readTree(meBJson).get("id").asLong();

        // A creates conversation with B
        String convJson = mockMvc.perform(post("/api/conversations")
                        .header("Authorization", "Bearer " + tA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otherUserId\":" + userBId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.otherUserId").value(userBId))
                .andReturn().getResponse().getContentAsString();
        Long convId = om.readTree(convJson).get("id").asLong();

        // A sends a message
        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + tA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello from A\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello from A"));

        // B sees it in their conversation list
        mockMvc.perform(get("/api/conversations").header("Authorization", "Bearer " + tB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));

        // B sees unread notification
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + tB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", greaterThanOrEqualTo(1)));
    }

    @Test
    void conversation_withSelf_rejected() throws Exception {
        String token = reg("self");
        String meJson = mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long myId = om.readTree(meJson).get("id").asLong();

        mockMvc.perform(post("/api/conversations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otherUserId\":" + myId + "}"))
                .andExpect(status().isBadRequest());
    }
}