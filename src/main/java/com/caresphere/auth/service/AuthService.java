package com.caresphere.auth.service;

import com.caresphere.auth.domain.Role;
import com.caresphere.auth.domain.User;
import com.caresphere.auth.dto.AuthResponse;
import com.caresphere.auth.dto.LoginRequest;
import com.caresphere.auth.dto.RegisterRequest;
import com.caresphere.auth.repository.UserRepository;
import com.caresphere.auth.security.JwtService;
import com.caresphere.common.audit.AuditService;
import com.caresphere.common.exception.ConflictException;
import com.caresphere.common.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Registration and login logic. Passwords are hashed with BCrypt and never stored or
 * logged in plain text.
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final long expirationMs;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuditService auditService,
                       @Value("${caresphere.jwt.expiration-ms:3600000}") long expirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.expirationMs = expirationMs;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered: " + request.email());
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(Role.CLINICIAN); // least privilege — self-registration cannot grant ADMIN
        user.setEnabled(true);
        userRepository.save(user);
        auditService.recordAs(request.email(), "REGISTER", "AUTH", user.getId(), null, "SUCCESS");
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null || !user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            // same message for unknown email vs. wrong password — prevents enumeration
            auditService.recordAs(request.email(), "LOGIN", "AUTH",
                    user == null ? null : user.getId(), null, "FAILURE");
            throw new InvalidCredentialsException();
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        auditService.recordAs(request.email(), "LOGIN", "AUTH", user.getId(), null, "SUCCESS");
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), user.getEmail(), user.getRole().name(), expirationMs);
    }
}
