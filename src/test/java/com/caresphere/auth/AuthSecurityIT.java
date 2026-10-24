package com.caresphere.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end security test against a throwaway PostgreSQL+pgvector container: admin
 * bootstrap, registration, login, duplicate detection, invalid credentials, and the
 * permitAll vs. authenticated boundary. Requires Docker; runs under {@code mvn verify}.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIT {

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper om;

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Test
    void bootstrapAdminCanLogin() throws Exception {
        login("admin@caresphere.dev", "Admin12345!")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void registerCreatesAccountAndLoginSucceeds() throws Exception {
        register("ituser1@caresphere.dev", "Password1!", "IT User One")
                .andExpect(status().isCreated());

        login("ituser1@caresphere.dev", "Password1!")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CLINICIAN"));
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        register("ituser2@caresphere.dev", "Password1!", "IT User Two")
                .andExpect(status().isCreated());
        register("ituser2@caresphere.dev", "Password1!", "IT User Two")
                .andExpect(status().isConflict());
    }

    @Test
    void loginRejectsBadPassword() throws Exception {
        register("ituser3@caresphere.dev", "Password1!", "IT User Three")
                .andExpect(status().isCreated());
        login("ituser3@caresphere.dev", "wrong-password")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthIsOpenButProtectedEndpointsRequireAuth() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions register(String email, String password, String fullName) throws Exception {
        return mvc.perform(post("/api/auth/register")
                .contentType(JSON)
                .content(om.writeValueAsString(java.util.Map.of(
                        "email", email, "password", password, "fullName", fullName))));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(JSON)
                .content(om.writeValueAsString(java.util.Map.of(
                        "email", email, "password", password))));
    }
}
