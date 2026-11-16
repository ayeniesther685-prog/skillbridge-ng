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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DisputesTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepo;
    @Autowired RoleRepository roleRepo;
    @Autowired JobRepository jobRepo;
    @Autowired ContractRepository contractRepo;
    @Autowired MilestoneRepository milestoneRepo;
    @Autowired DisputeRepository disputeRepo;
    @Autowired PasswordEncoder encoder;

    private String clientToken;
    private String freelancerToken;
    private String adminToken;
    private Long contractId;
    private Long milestoneId;

    @BeforeEach
    void setup() throws Exception {
        String cEmail = "c-" + System.nanoTime() + "@example.com";
        String fEmail = "f-" + System.nanoTime() + "@example.com";
        clientToken = register(cEmail, RoleName.CLIENT);
        freelancerToken = register(fEmail, RoleName.FREELANCER);

        User client = userRepo.findByEmailIgnoreCase(cEmail).orElseThrow();
        User freelancer = userRepo.findByEmailIgnoreCase(fEmail).orElseThrow();
        markVerified(client);
        markVerified(freelancer);

        // Build admin directly and log in
        String adminEmail = "admin-" + System.nanoTime() + "@example.com";
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("One");
        admin.setEmail(adminEmail);
        admin.setPassword(encoder.encode("AdminPass123"));
        admin.setActive(true);
        admin.setEmailVerified(true);
        Role adminRole = roleRepo.findByName(RoleName.ADMIN).orElseThrow();
        admin.addRole(adminRole);
        userRepo.save(admin);

        String loginBody = "{\"email\":\"" + adminEmail + "\",\"password\":\"AdminPass123\"}";
        var loginRes = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk()).andReturn();
        adminToken = om.readTree(loginRes.getResponse().getContentAsString()).get("accessToken").asText();

        // Build job + contract + milestone
        Job job = new Job();
        job.setClient(client);
        job.setTitle("Test job");
        job.setDescription("desc");
        job.setJobType(JobType.FREELANCE);
        job.setExperienceLevel(ExperienceLevel.ENTRY);
        job.setStatus(JobStatus.IN_PROGRESS);
        job = jobRepo.save(job);

        Contract contract = new Contract();
        contract.setJob(job);
        contract.setClient(client);
        contract.setFreelancer(freelancer);
        contract.setTitle("Dispute test contract");
        contract.setAmount(new BigDecimal("1000.00"));
        contract.setStatus(ContractStatus.ACTIVE);
        contract = contractRepo.save(contract);
        contractId = contract.getId();

        Milestone m = new Milestone();
        m.setContract(contract);
        m.setTitle("Milestone 1");
        m.setAmount(new BigDecimal("500.00"));
        m.setStatus(MilestoneStatus.SUBMITTED);
        m.setRevisionLimit(3);
        m.setRevisionCount(0);
        m = milestoneRepo.save(m);
        milestoneId = m.getId();
    }

    private void markVerified(User u) {
        u.setEmailVerified(true);
        userRepo.save(u);
    }

    private String register(String email, RoleName role) throws Exception {
        String body = """
            {"firstName":"A","lastName":"B","email":"%s","password":"TestPass123","phone":"08000000000","roles":["%s"]}
            """.formatted(email, role.name());
        var res = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return om.readTree(res.getResponse().getContentAsString()).get("accessToken").asText();
    }

    // ------------------ tests ------------------

    @Test
    void client_canOpenDispute() throws Exception {
        String body = """
            {"reason":"Freelancer did not deliver the agreed milestone, please review."}
            """;
        mvc.perform(post("/api/contracts/" + contractId + "/disputes")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.contractId").value(contractId.intValue()));
    }

    @Test
    void freelancer_canOpenDispute() throws Exception {
        String body = """
            {"reason":"Client is refusing to accept deliverables that match the brief."}
            """;
        mvc.perform(post("/api/contracts/" + contractId + "/disputes")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void openingDispute_blocksMilestoneApproval() throws Exception {
        // Open dispute
        mvc.perform(post("/api/contracts/" + contractId + "/disputes")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Something went wrong with this milestone.\"}"))
                .andExpect(status().isCreated());

        // Attempt to APPROVE milestone
        mvc.perform(put("/api/milestones/" + milestoneId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("dispute")));
    }

    @Test
    void admin_canResolve_andUnblocksApproval() throws Exception {
        // Open
        var openRes = mvc.perform(post("/api/contracts/" + contractId + "/disputes")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Something went wrong with this milestone.\"}"))
                .andExpect(status().isCreated()).andReturn();
        Long disputeId = om.readTree(openRes.getResponse().getContentAsString()).get("id").asLong();

        // Resolve
        mvc.perform(post("/api/admin/disputes/" + disputeId + "/resolve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"RESOLVED_FOR_CLIENT\",\"resolution\":\"Client is right, deliverable was missing.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED_FOR_CLIENT"));

        // Approval should now work
        mvc.perform(put("/api/milestones/" + milestoneId + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void secondOpenDispute_rejected() throws Exception {
        mvc.perform(post("/api/contracts/" + contractId + "/disputes")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"First dispute reason is quite detailed.\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/contracts/" + contractId + "/disputes")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Second dispute attempt should be blocked.\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void stranger_cannotListDisputes() throws Exception {
        // Register a student (unrelated)
        String sEmail = "s-" + System.nanoTime() + "@example.com";
        String studentToken = register(sEmail, RoleName.STUDENT);
        User s = userRepo.findByEmailIgnoreCase(sEmail).orElseThrow();
        markVerified(s);

        // Re-login to get verified token
        var loginRes = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + sEmail + "\",\"password\":\"TestPass123\"}"))
                .andExpect(status().isOk()).andReturn();
        String verifiedStudentToken = om.readTree(loginRes.getResponse().getContentAsString())
                .get("accessToken").asText();

        mvc.perform(get("/api/contracts/" + contractId + "/disputes")
                        .header("Authorization", "Bearer " + verifiedStudentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdmin_cannotAccessAdminList() throws Exception {
        mvc.perform(get("/api/admin/disputes")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }
}
