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
class AcademyTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    private String register(String prefix, RoleName role) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        RegisterRequest r = new RegisterRequest(
                "T", "U", email, "Password123", null, Set.of(role));
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(r)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("accessToken").asText();
    }

    private long findSeedProgramId() throws Exception {
        String json = mockMvc.perform(get("/api/academy/programs"))
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get(0).get("id").asLong();
    }

    private long findSeedDepartmentId(long programId) throws Exception {
        String json = mockMvc.perform(get("/api/academy/departments?programId=" + programId))
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get(0).get("id").asLong();
    }

    @Test
    void publicCanListPrograms() throws Exception {
        mockMvc.perform(get("/api/academy/programs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].code").value("SWE"));
    }

    @Test
    void publicCanListCoursesForDepartment() throws Exception {
        long programId = findSeedProgramId();
        long departmentId = findSeedDepartmentId(programId);
        mockMvc.perform(get("/api/academy/courses?departmentId=" + departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[?(@.code=='SWE101')]", not(empty())));
    }

    @Test
    void unauthenticatedCannotEnroll() throws Exception {
        long programId = findSeedProgramId();
        mockMvc.perform(post("/api/academy/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + programId + "}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCanEnrollAndSeeRecord() throws Exception {
        String token = register("student", RoleName.STUDENT);
        long programId = findSeedProgramId();
        long deptId = findSeedDepartmentId(programId);

        mockMvc.perform(post("/api/academy/enroll")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + programId + ",\"departmentId\":" + deptId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId", startsWith("SB/")))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/academy/me/record")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.programId").value(programId));
    }

    @Test
    void cannotEnrollTwice() throws Exception {
        String token = register("twice", RoleName.STUDENT);
        long programId = findSeedProgramId();

        String body = "{\"programId\":" + programId + "}";
        mockMvc.perform(post("/api/academy/enroll")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/academy/enroll")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void studentCanEnrollInCourse() throws Exception {
        String token = register("coursetaker", RoleName.STUDENT);
        long programId = findSeedProgramId();
        long deptId = findSeedDepartmentId(programId);

        mockMvc.perform(post("/api/academy/enroll")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"programId\":" + programId + ",\"departmentId\":" + deptId + "}"))
                .andExpect(status().isCreated());

        // get a course id and a semester id
        String coursesJson = mockMvc.perform(get("/api/academy/courses?departmentId=" + deptId))
                .andReturn().getResponse().getContentAsString();
        long courseId = om.readTree(coursesJson).get(0).get("id").asLong();

        String sessionsJson = mockMvc.perform(get("/api/academy/sessions"))
                .andReturn().getResponse().getContentAsString();
        long sessionId = om.readTree(sessionsJson).get(0).get("id").asLong();

        String semestersJson = mockMvc.perform(get("/api/academy/sessions/" + sessionId + "/semesters"))
                .andReturn().getResponse().getContentAsString();
        long semesterId = om.readTree(semestersJson).get(0).get("id").asLong();

        mockMvc.perform(post("/api/academy/me/enrollments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + courseId + ",\"semesterId\":" + semesterId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ENROLLED"));
    }

    @Test
    void nonAdminCannotCreateProgram() throws Exception {
        String token = register("notadmin", RoleName.STUDENT);
        mockMvc.perform(post("/api/admin/academy/programs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"code\":\"TST\"}"))
                .andExpect(status().isForbidden());
    }
}