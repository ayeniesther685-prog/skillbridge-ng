package org.skillbridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.skillbridge.dto.request.RegisterRequest;
import org.skillbridge.entity.ProviderVerification;
import org.skillbridge.entity.User;
import org.skillbridge.enums.ProviderType;
import org.skillbridge.enums.RoleName;
import org.skillbridge.enums.VerificationStatus;
import org.skillbridge.repository.ProviderVerificationRepository;
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
class JobRbacTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepository;
    @Autowired ProviderVerificationRepository providerVerificationRepository;

    private String register(String prefix, RoleName role) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        RegisterRequest r = new RegisterRequest(
                "R", "Test", email, "Password123", null, Set.of(role));
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(r)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("accessToken").asText();
    }

    /**
     * Phase 1 Part B: since V26, a CLIENT must be a VERIFIED provider before
     * POST /api/jobs succeeds. These tests exercise job RBAC, not verification,
     * so we set up the verification precondition directly via the repository.
     */
    private void markClientVerified(String email) {
        User u = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        ProviderVerification pv = providerVerificationRepository.findByUserId(u.getId())
                .orElseGet(() -> {
                    ProviderVerification fresh = new ProviderVerification();
                    fresh.setUser(u);
                    return fresh;
                });
        pv.setProviderType(ProviderType.EMPLOYER);
        pv.setLegalName("Test Client Ltd");
        pv.setOfficialEmail(email);
        pv.setOfficialPhone("08000000000");
        pv.setPhysicalAddress("Test Address");
        pv.setAuthorizedRepName("Test Rep");
        pv.setAuthorizedRepRole("Owner");
        pv.setStatus(VerificationStatus.VERIFIED);
        providerVerificationRepository.save(pv);
    }

    /**
     * Overload that returns the token AND marks the account verified.
     * Use this in tests that need to create a job as the registered CLIENT.
     */
    private String registerVerifiedClient(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        RegisterRequest r = new RegisterRequest(
                "R", "Test", email, "Password123", null, Set.of(RoleName.CLIENT));
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(r)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        markClientVerified(email);
        return om.readTree(json).get("accessToken").asText();
    }

    private static final String JOB_BODY = """
            {
              "title": "Test Job",
              "description": "test description",
              "jobType": "FREELANCE",
              "experienceLevel": "ENTRY"
            }
            """;

    private static final String APPLY_BODY = """
            {
              "proposal": {
                "coverLetter": "I can do this",
                "proposedPrice": 500
              }
            }
            """;

    @Test
    void unauthenticated_cannotCreateJob() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void student_cannotCreateJob() throws Exception {
        String token = register("student", RoleName.STUDENT);
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void freelancer_cannotCreateJob() throws Exception {
        String token = register("freelancer", RoleName.FREELANCER);
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void client_canCreateJob() throws Exception {
        String token = registerVerifiedClient("client");
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void client_cannotUpdateAnotherClientsJob() throws Exception {
        String ownerToken = registerVerifiedClient("clientA");
        String otherToken = registerVerifiedClient("clientB");

        String jobJson = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long jobId = om.readTree(jobJson).get("id").asLong();

        mockMvc.perform(put("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void client_cannotDeleteAnotherClientsJob() throws Exception {
        String ownerToken = registerVerifiedClient("clientC");
        String otherToken = registerVerifiedClient("clientD");

        String jobJson = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long jobId = om.readTree(jobJson).get("id").asLong();

        mockMvc.perform(delete("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicCanListJobs() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void freelancer_canApply() throws Exception {
        String clientToken = registerVerifiedClient("clientE");
        String freelancerToken = register("freelancerE", RoleName.FREELANCER);

        String jobJson = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long jobId = om.readTree(jobJson).get("id").asLong();

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPLY_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void student_canApply() throws Exception {
        String clientToken = registerVerifiedClient("clientF");
        String studentToken = register("studentF", RoleName.STUDENT);

        String jobJson = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long jobId = om.readTree(jobJson).get("id").asLong();

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPLY_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void client_cannotApplyToAnyJob() throws Exception {
        String ownerToken = registerVerifiedClient("clientG");
        String otherClientToken = register("clientH", RoleName.CLIENT);

        String jobJson = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long jobId = om.readTree(jobJson).get("id").asLong();

        mockMvc.perform(post("/api/jobs/" + jobId + "/applications")
                        .header("Authorization", "Bearer " + otherClientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPLY_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRegistrationBlocked() throws Exception {
        String email = "hacker-" + UUID.randomUUID() + "@example.com";
        RegisterRequest req = new RegisterRequest(
                "Hacker", "Test", email, "Password123", null, Set.of(RoleName.ADMIN));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unverifiedClient_cannotCreateJob() throws Exception {
        String token = register("unverifiedClient", RoleName.CLIENT);
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JOB_BODY))
                .andExpect(status().isForbidden());
    }
}
