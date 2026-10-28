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
class Phase4Tests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    private String registerClient(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        RegisterRequest reg = new RegisterRequest(
                "Test", "Client", email, "Password123", null, Set.of(RoleName.CLIENT));
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("accessToken").asText();
    }

    private String registerFreelancer(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        RegisterRequest reg = new RegisterRequest(
                "Test", "Freelancer", email, "Password123", null, Set.of(RoleName.FREELANCER));
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("accessToken").asText();
    }

    @Test
    void fullJobApplicationFlow() throws Exception {
        String clientToken = registerClient("client");
        String freelancerToken = registerFreelancer("freelancer");

        // 1. Client posts a job
        String jobJson = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Build a REST API","description":"Need a Spring Boot API",
                                 "budgetMin":500,"budgetMax":1500,"location":"Lagos",
                                 "jobType":"FREELANCE","experienceLevel":"INTERMEDIATE",
                                 "skillIds":[1,2]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Build a REST API"))
                .andExpect(jsonPath("$.requiredSkills", hasSize(2)))
                .andReturn().getResponse().getContentAsString();

        Long jobId = om.readTree(jobJson).get("id").asLong();

        // 2. Freelancer applies
        String appJson = mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"expectedBudget":1000,
                                 "proposal":{"coverLetter":"I can do this","proposedPrice":1000,"estimatedDeliveryDays":14}}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.proposal.proposedPrice").value(1000))
                .andReturn().getResponse().getContentAsString();

        Long appId = om.readTree(appJson).get("id").asLong();

        // 3. Client shortlists
        mockMvc.perform(put("/api/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHORTLISTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));

        // 4. Client accepts
        mockMvc.perform(put("/api/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // 5. Invalid transition — ACCEPTED is terminal
        mockMvc.perform(put("/api/applications/" + appId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void publicJobsListing_noAuthRequired() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void applyingToOwnJob_rejected() throws Exception {
        String clientToken = registerClient("owner");

        String jobJson = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Self test","description":"Nope",
                                 "jobType":"FREELANCE","experienceLevel":"ENTRY"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long jobId = om.readTree(jobJson).get("id").asLong();

        // CLIENTs can't apply to any job. The service layer enforces this before
        // the "own job" check, so we get 403 (forbidden) rather than 400.
        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"proposal":{"coverLetter":"me","proposedPrice":100}}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateApplication_rejected() throws Exception {
        String clientToken = registerClient("client2");
        String freelancerToken = registerFreelancer("freelancer2");

        String jobJson = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Test","description":"Test",
                                 "jobType":"FREELANCE","experienceLevel":"ENTRY"}
                                """))
                .andReturn().getResponse().getContentAsString();
        Long jobId = om.readTree(jobJson).get("id").asLong();

        String payload = """
                {"proposal":{"coverLetter":"x","proposedPrice":100}}
                """;

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict());
    }
}