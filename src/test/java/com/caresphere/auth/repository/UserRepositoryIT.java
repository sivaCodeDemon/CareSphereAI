package com.caresphere.auth.repository;

import com.caresphere.auth.domain.Role;
import com.caresphere.auth.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: boots the full app against a throwaway PostgreSQL+pgvector container.
 * Verifies that Flyway migrations apply, Hibernate mappings validate, and the repository
 * round-trips a user. Requires Docker; runs under {@code mvn verify}.
 */
@Testcontainers
@SpringBootTest
class UserRepositoryIT {

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @Autowired
    private UserRepository userRepository;

    @Test
    void persistsUserAndFindsByEmail() {
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("it-test@caresphere.dev");
        user.setPasswordHash("hashed-secret");
        user.setFullName("IT Test User");
        user.setRole(Role.CLINICIAN);
        user.setEnabled(true);

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        assertThat(userRepository.findByEmail("it-test@caresphere.dev")).isPresent();
        assertThat(userRepository.existsByEmail("it-test@caresphere.dev")).isTrue();
    }
}
