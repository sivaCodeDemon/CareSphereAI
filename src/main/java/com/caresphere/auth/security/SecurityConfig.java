package com.caresphere.auth.security;

import com.caresphere.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * Stateless security: JWT-based auth, role-based authorization, CORS, and JSON 401/403
 * responses consistent with the {@link ErrorResponse} envelope used across the API.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter, ObjectMapper objectMapper)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // REST API, no cookies; CSRF not applicable
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/auth/**",
                        "/api/health/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**"
                ).permitAll()
                .requestMatchers("/api/patients/**", "/api/documents/**", "/api/ai/**").hasAnyRole("CLINICIAN", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint((req, res, ex) ->
                        writeError(req, res, objectMapper, HttpStatus.UNAUTHORIZED, "Unauthorized",
                                "Authentication is required"))
                .accessDeniedHandler((req, res, ex) ->
                        writeError(req, res, objectMapper, HttpStatus.FORBIDDEN, "Forbidden",
                                "You do not have permission to access this resource")))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeError(HttpServletRequest req, HttpServletResponse res, ObjectMapper mapper,
                            HttpStatus status, String error, String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), error, message,
                req.getRequestURI(), List.of());
        res.getWriter().write(mapper.writeValueAsString(body));
    }
}
