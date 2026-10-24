package com.caresphere.auth.service;

import com.caresphere.auth.domain.Role;
import com.caresphere.auth.domain.User;
import com.caresphere.auth.dto.LoginRequest;
import com.caresphere.auth.dto.RegisterRequest;
import com.caresphere.auth.repository.UserRepository;
import com.caresphere.auth.security.JwtService;
import com.caresphere.common.exception.ConflictException;
import com.caresphere.common.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private com.caresphere.common.audit.AuditService auditService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, auditService, 3600000);
    }

    @Test
    void registerCreatesClinicianAndReturnsToken() {
        when(userRepository.existsByEmail("new@caresphere.dev")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        var response = authService.register(new RegisterRequest("new@caresphere.dev", "Password1", "New User"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("new@caresphere.dev");
        assertThat(response.role()).isEqualTo("CLINICIAN");
        assertThat(response.expiresInMs()).isEqualTo(3600000);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("dup@caresphere.dev")).thenReturn(true);
        assertThatThrownBy(() -> authService.register(new RegisterRequest("dup@caresphere.dev", "Password1", "Dup")))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginSucceedsWithValidCredentials() {
        User user = userWith("valid@caresphere.dev", "hash", true, Role.CLINICIAN);
        when(userRepository.findByEmail("valid@caresphere.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1", "hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("valid@caresphere.dev", "Password1"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo("CLINICIAN");
        verify(userRepository).save(user); // records last login
    }

    @Test
    void loginFailsForUnknownEmail() {
        when(userRepository.findByEmail("missing@caresphere.dev")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@caresphere.dev", "Password1")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginFailsForWrongPassword() {
        User user = userWith("valid@caresphere.dev", "hash", true, Role.CLINICIAN);
        when(userRepository.findByEmail("valid@caresphere.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("valid@caresphere.dev", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginFailsForDisabledAccount() {
        User user = userWith("valid@caresphere.dev", "hash", false, Role.CLINICIAN);
        when(userRepository.findByEmail("valid@caresphere.dev")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("valid@caresphere.dev", "Password1")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private User userWith(String email, String passwordHash, boolean enabled, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setEnabled(enabled);
        user.setRole(role);
        return user;
    }
}
