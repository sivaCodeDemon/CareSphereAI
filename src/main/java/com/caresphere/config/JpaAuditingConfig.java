package com.caresphere.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so {@code @CreatedDate} / {@code @LastModifiedDate} are populated
 * automatically on audited entities.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
