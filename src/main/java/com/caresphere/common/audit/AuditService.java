package com.caresphere.common.audit;

import com.caresphere.auth.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records security-relevant domain events. Writes happen in a separate transaction
 * ({@link Propagation#REQUIRES_NEW}) and never fail the originating business operation.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;
    private final AuthenticatedUser authenticatedUser;

    public AuditService(AuditEventRepository auditEventRepository, AuthenticatedUser authenticatedUser) {
        this.auditEventRepository = auditEventRepository;
        this.authenticatedUser = authenticatedUser;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String resourceType, Long resourceId, Long patientId, String status) {
        recordAs(authenticatedUser.getUsername(), action, resourceType, resourceId, patientId, status);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAs(String actor, String action, String resourceType, Long resourceId, Long patientId, String status) {
        try {
            AuditEvent event = new AuditEvent();
            event.setActor(actor);
            event.setAction(action);
            event.setResourceType(resourceType);
            event.setResourceId(resourceId);
            event.setPatientId(patientId);
            event.setStatus(status);
            auditEventRepository.save(event);
        } catch (RuntimeException e) {
            // Auditing must never break the business operation it observes.
            log.warn("Failed to persist audit event for action={}: {}", action, e.getMessage());
        }
    }
}
