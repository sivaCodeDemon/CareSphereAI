package com.caresphere.patient.controller;

import com.caresphere.auth.repository.UserRepository;
import com.caresphere.auth.security.CustomUserDetailsService;
import com.caresphere.auth.security.JwtAuthenticationFilter;
import com.caresphere.auth.security.JwtService;
import com.caresphere.auth.security.SecurityConfig;
import com.caresphere.config.CorsConfig;
import com.caresphere.patient.dto.PatientResponse;
import com.caresphere.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class,
        CustomUserDetailsService.class, CorsConfig.class})
class PatientControllerTest {

    @Autowired private MockMvc mvc;

    @MockitoBean private PatientService patientService;
    @MockitoBean private UserRepository userRepository;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mvc.perform(get("/api/patients")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLINICIAN")
    void listReturnsPageForClinician() throws Exception {
        PatientResponse p = new PatientResponse(1L, "MRN-1", "Jane", "Doe",
                LocalDate.of(1990, 1, 1), "FEMALE", "j@e.com", "555", "addr",
                "clinician@caresphere.dev", Instant.now(), Instant.now());
        when(patientService.list(any(), any()))
                .thenReturn(new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1));

        mvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].mrn").value("MRN-1"));
    }

    @Test
    @WithMockUser(roles = "CLINICIAN")
    void createRejectsInvalidPayload() throws Exception {
        mvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mrn":"","firstName":"","lastName":"","dateOfBirth":"2099-01-01"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
