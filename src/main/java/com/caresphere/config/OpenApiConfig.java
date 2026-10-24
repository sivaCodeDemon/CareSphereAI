package com.caresphere.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized OpenAPI metadata. The generated docs are served at /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI caresphereOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CareSphere AI API")
                        .description("Secure, AI-powered healthcare document intelligence platform. "
                                + "Educational portfolio project — not a medical diagnostic system.")
                        .version("0.1.0")
                        .license(new License().name("MIT").url("https://opensource.org/license/mit")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
