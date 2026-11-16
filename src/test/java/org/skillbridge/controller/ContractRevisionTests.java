package org.skillbridge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skillbridge.entity.*;
import org.skillbridge.enums.*;
import org.skillbridge.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContractRevisionTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepo;
    @Autowired RoleRepository roleRepo;
    @Autowired PasswordEncoder encoder;
    @Autowired JobRepository jobRepo;
    @Autowired ContractRepository contractRepo;
    @Autowired MilestoneRepository milestoneRepo;

    private String clientToken, freelancerToken;
    private Long contractId, milestoneId;

    @BeforeEach
    void setup() throws Exception {
        // CLIENT + FREELANCER
        String cEmail = "client-" + System.nanoTime() + "@example.com";
        String fEmail = "free-" + System.nanoTime() + "@example.com";
        clientToken = register(cEmail, RoleName.CLIENT, "ClientPass123");
        freelancerToken = register(fEmail, RoleName.FREELANCER, "FreePass123");

        User client = userRepo.findByEmailIgnoreCase(cEmail).orElseThrow();
        User freelancer = userRepo.findByEmailIgnoreCase(fEmail).orElseThrow();

        // Minimal Job
        Job job = new Job();
        job.setClient(client);
        job.setTitle("Test job");
        job.setDescription("desc");
        job.setJobType(JobType.FREELANCE);
        job.setExperienceLevel(ExperienceLevel.ENTRY);
        job.setStatus(JobStatus.IN_PROGRESS);
        job = jobRepo.save(job);

        // Contract in ACTIVE
        Contract contract = new Contract();
        contract.setJob(job);
        contract.setClient(client);
        contract.setFreelancer(freelancer);
        contract.setTitle("Test contract");
        contract.setAmount(new BigDecimal("1000.00"));
        contract.setStatus(ContractStatus.ACTIVE);
        contract = contractRepo.save(contract);
        contractId = contract.getId();

        // Milestone in SUBMITTED (ready for revision)
        Milestone m = new Milestone();
        m.setContract(contract);
        m.setTitle("Milestone 1");
        m.setDescription("Original requirements");
        m.setAmount(new BigDecimal("500.00"));
        m.setStatus(MilestoneStatus.SUBMITTED);
        m.setRevisionLimit(3);
        m.setRevisionCount(0);
        m = milestoneRepo.save(m);
        milestoneId = m.getId();
    }

    private String register(String email, RoleName role, String pw) throws Exception {
        String body = """
            {"firstName":"A","lastName":"B","email":"%s","password":"%s","phone":"08000000000","roles":["%s"]}
            """.formatted(email, pw, role.name());
        var res = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return om.readTree(res.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private Long createRevision(String reason, boolean isNew) throws Exception {
        String body = """
            {"reason":"%s","isNewRequirements":%s}
            """.formatted(reason, isNew);
        var res = mvc.perform(post("/api/milestones/" + milestoneId + "/revisions")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        JsonNode json = om.readTree(res.getResponse().getContentAsString());
        return json.has("id") ? json.get("id").asLong() : null;
    }

    @Test
    void client_canRequestRevision() throws Exception {
        String body = """
            {"reason":"Please fix the API contract","isNewRequirements":false}
            """;
        mvc.perform(post("/api/milestones/" + milestoneId + "/revisions")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.milestoneId").value(milestoneId.intValue()));

        // Milestone should now be REVISION_REQUESTED and count=1
        Milestone m = milestoneRepo.findById(milestoneId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(MilestoneStatus.REVISION_REQUESTED, m.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(1, m.getRevisionCount());
    }

    @Test
    void freelancer_canRespond() throws Exception {
        Long revId = createRevision("Please fix", false);
        org.junit.jupiter.api.Assertions.assertNotNull(revId);

        String body = """
            {"accept":true,"response":"I will fix today"}
            """;
        mvc.perform(post("/api/revisions/" + revId + "/respond")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        Milestone m = milestoneRepo.findById(milestoneId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(MilestoneStatus.IN_PROGRESS, m.getStatus());
    }

    @Test
    void freelancer_cannotRequestRevision() throws Exception {
        String body = """
            {"reason":"Please fix","isNewRequirements":false}
            """;
        mvc.perform(post("/api/milestones/" + milestoneId + "/revisions")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void client_cannotRespond() throws Exception {
        Long revId = createRevision("Please fix", false);
        String body = """
            {"accept":true,"response":"OK"}
            """;
        mvc.perform(post("/api/revisions/" + revId + "/respond")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void revisionLimitExceeded_returns409() throws Exception {
        Milestone m = milestoneRepo.findById(milestoneId).orElseThrow();
        m.setRevisionCount(3);
        milestoneRepo.save(m);

        String body = """
            {"reason":"Please fix","isNewRequirements":false}
            """;
        mvc.perform(post("/api/milestones/" + milestoneId + "/revisions")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Revision limit")));
    }

    @Test
    void newRequirements_returns409_andDoesNotConsumeSlot() throws Exception {
        String body = """
            {"reason":"Add a whole new feature","isNewRequirements":true}
            """;
        mvc.perform(post("/api/milestones/" + milestoneId + "/revisions")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("new milestone")));

        Milestone m = milestoneRepo.findById(milestoneId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(0, m.getRevisionCount());
        org.junit.jupiter.api.Assertions.assertEquals(MilestoneStatus.SUBMITTED, m.getStatus());
    }

    @Test
    void listForContract_returnsRevisions() throws Exception {
        createRevision("Fix one", false);
        mvc.perform(get("/api/contracts/" + contractId + "/revisions")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listForContract_forbiddenForStranger() throws Exception {
        String stranger = register("stranger-" + System.nanoTime() + "@example.com", RoleName.STUDENT, "StrPass123");
        mvc.perform(get("/api/contracts/" + contractId + "/revisions")
                        .header("Authorization", "Bearer " + stranger))
                .andExpect(status().isForbidden());
    }
}
