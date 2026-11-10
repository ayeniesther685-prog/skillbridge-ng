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
class LessonTests {

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
    void publicCanListLessonsForCourse() throws Exception {
        // Lesson list is public preview — no auth needed
        mockMvc.perform(get("/api/academy/courses/1/lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.lessons", hasSize(3)))
                .andExpect(jsonPath("$.lessons[0].title").value("Variables, Types, and Operators"));
    }

    @Test
    void publicCanViewSingleLesson() throws Exception {
        mockMvc.perform(get("/api/academy/courses/1/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content", containsString("primitive types")));
    }

    @Test
    void studentCanTrackProgressAfterEnrollment() throws Exception {
        String token = register("prog");
        enrollInProgramAndCourse(token);

        // Mark lesson 1 in progress
        mockMvc.perform(put("/api/academy/lessons/1/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonId").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Mark lesson 1 complete
        mockMvc.perform(put("/api/academy/lessons/1/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt", notNullValue()));

        // Check progress report
        mockMvc.perform(get("/api/academy/me/courses/1/progress")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLessons").value(3))
                .andExpect(jsonPath("$.completedLessons").value(1))
                .andExpect(jsonPath("$.percentComplete").value(33));
    }

    @Test
    void cannotTrackProgressWithoutEnrollment() throws Exception {
        String token = register("notenrolled");

        mockMvc.perform(put("/api/academy/lessons/1/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannotUncompleteCompletedLesson() throws Exception {
        String token = register("completer");
        enrollInProgramAndCourse(token);

        mockMvc.perform(put("/api/academy/lessons/2/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/academy/lessons/2/progress")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void nonAdminCannotCreateLesson() throws Exception {
        String token = register("notadmin");

        mockMvc.perform(post("/api/admin/academy/courses/1/lessons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"content\":\"Body\",\"orderNumber\":99}"))
                .andExpect(status().isForbidden());
    }
}