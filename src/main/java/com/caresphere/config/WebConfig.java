package com.caresphere.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Stable, DTO-based serialization of Spring Data {@code Page} responses (PagedModel) so the
 * pagination JSON contract is stable and OpenAPI-friendly, instead of the raw PageImpl shape.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig {
}
