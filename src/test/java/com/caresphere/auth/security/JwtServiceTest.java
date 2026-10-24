package com.caresphere.auth.security;

import com.caresphere.auth.domain.Role;
import com.caresphere.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        // secret must be >= 32 bytes for HS256
        jwtService = new JwtService("test-secret-test-secret-test-secret-test-secret-32bytes!", 3600000);
        user = new User();
        user.setEmail("tester@caresphere.dev");
        user.setRole(Role.ADMIN);
    }

    private UserDetails userDetails(String username, String role) {
        return org.springframework.security.core.userdetails.User.withUsername(username)
                .password("hash")
                .roles(role)
                .build();
    }

    @Test
    void generatedTokenCarriesTheUsername() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.extractUsername(token)).isEqualTo("tester@caresphere.dev");
    }

    @Test
    void validTokenPassesValidation() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, userDetails("tester@caresphere.dev", "ADMIN"))).isTrue();
    }

    @Test
    void tamperedTokenIsInvalid() {
        String token = jwtService.generateToken(user) + "tamper";
        assertThat(jwtService.isTokenValid(token, userDetails("tester@caresphere.dev", "ADMIN"))).isFalse();
    }

    @Test
    void tokenForADifferentUserIsInvalid() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, userDetails("someone-else@caresphere.dev", "ADMIN"))).isFalse();
    }
}
