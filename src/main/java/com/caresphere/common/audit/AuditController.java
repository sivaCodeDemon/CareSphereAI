package com.caresphere.common.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Audit", description = "Security audit log (administrators only)")
@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Operation(summary = "List audit events (newest first, paginated)")
    @GetMapping
    public Page<AuditEvent> list(Pageable pageable) {
        return auditEventRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
