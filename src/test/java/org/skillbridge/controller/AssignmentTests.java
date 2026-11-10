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
class AssignmentTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    private String register(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        RegisterRequest r = new RegisterRequest(
                "T", "U", email, "Password123", null, Set.of(RoleName.STUDENT));
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(r)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("accessToken").asText();
    }

    private void enrollInProgramAndCourse(String token) throws Exception {
        mockMvc.perform(post("/api/academy/enroll")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":1,\"departmentId\":1}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/academy/me/enrollments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1,\"semesterId\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void publicCanListPublishedAssignmentsForCourse() throws Exception {
        mockMvc.perform(get("/api/academy/courses/1/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].title").value("FizzBuzz in Java"))
                .andExpect(jsonPath("$[0].published").value(true));
    }

    @Test
    void publicCanViewSinglePublishedAssignment() throws Exception {
        // The seed assignment ID should be 1
        mockMvc.perform(get("/api/academy/assignments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("FizzBuzz in Java"))
                .andExpect(jsonPath("$.instructions", containsString("FizzBuzz")));
    }

    @Test
    void enrolledStudentCanSubmitAssignment() throws Exception {
        String token = register("submitter");
        enrollInProgramAndCourse(token);

        mockMvc.perform(post("/api/academy/assignments/1/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"public class FizzBuzz { public static void main(String[] a) {} }"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value(1))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.score").doesNotExist());
    }

    @Test
    void nonEnrolledUserCannotSubmit() throws Exception {
        String token = register("outsider");

        mockMvc.perform(post("/api/academy/assignments/1/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"my solution\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentCannotSubmitTwice() throws Exception {
        String token = register("double");
        enrollInProgramAndCourse(token);

        String body = "{\"content\":\"first attempt\"}";

        mockMvc.perform(post("/api/academy/assignments/1/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/academy/assignments/1/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void nonAdminCannotListAllSubmissions() throws Exception {
        String token = register("notadmin");

        mockMvc.perform(get("/api/admin/academy/assignments/1/submissions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}