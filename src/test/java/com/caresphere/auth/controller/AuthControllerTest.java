package com.caresphere.auth.controller;

import com.caresphere.auth.dto.AuthResponse;
import com.caresphere.auth.repository.UserRepository;
import com.caresphere.auth.security.CustomUserDetailsService;
import com.caresphere.auth.security.JwtAuthenticationFilter;
import com.caresphere.auth.security.JwtService;
import com.caresphere.auth.security.SecurityConfig;
import com.caresphere.auth.service.AuthService;
import com.caresphere.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sliced controller test. The real {@link SecurityConfig} is imported so the permitAll
 * rules and the 401 entry point are exercised against MockMvc without a database.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class,
        CustomUserDetailsService.class, CorsConfig.class})
class AuthControllerTest {

    @Autowired private MockMvc mvc;

    @MockitoBean private AuthService authService;
    @MockitoBean private UserRepository userRepository; // required by CustomUserDetailsService

    @Test
    void registerReturnsCreatedWithToken() throws Exception {
        when(authService.register(any()))
                .thenReturn(new AuthResponse("jwt", "new@caresphere.dev", "CLINICIAN", 3600000));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new@caresphere.dev","password":"Password1","fullName":"New User"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.role").value("CLINICIAN"));
    }

    @Test
    void registerRejectsInvalidPayload() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"short","fullName":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsOkWithToken() throws Exception {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("jwt", "u@caresphere.dev", "CLINICIAN", 3600000));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"u@caresphere.dev","password":"Password1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }
}
