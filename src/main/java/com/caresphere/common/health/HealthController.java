package com.caresphere.common.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Simple liveness/readiness endpoint for load balancers and container probes.
 */
@Tag(name = "Health", description = "Service health and readiness")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "Liveness probe")
    @GetMapping
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "caresphere-ai",
                "timestamp", Instant.now().toString()
        );
    }
}
