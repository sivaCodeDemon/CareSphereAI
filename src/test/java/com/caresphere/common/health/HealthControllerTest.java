package com.caresphere.common.health;

import com.caresphere.auth.repository.UserRepository;
import com.caresphere.auth.security.CustomUserDetailsService;
import com.caresphere.auth.security.JwtAuthenticationFilter;
import com.caresphere.auth.security.JwtService;
import com.caresphere.auth.security.SecurityConfig;
import com.caresphere.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sliced web test for the health endpoint. The real {@link SecurityConfig} is imported so
 * the {@code permitAll} rule for {@code /api/health} is genuinely exercised; no database is
 * loaded ({@link UserRepository} is mocked for the security filter chain's user details).
 */
@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class,
        CustomUserDetailsService.class, CorsConfig.class})
class HealthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void healthReturnsUpStatus() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("caresphere-ai"));
    }
}
