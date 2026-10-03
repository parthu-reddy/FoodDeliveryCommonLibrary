package com.fooddelivery.common.audit;

import com.fooddelivery.common.enums.AuditAction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Insert-only. MANDATORY prevents an audit row from committing independently of its action. */
@Component
public class AuditTrail {
    @PersistenceContext private EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public UUID record(Instant now, UUID actor, String kind, AuditAction action, String subjectType,
                       UUID subjectId, UUID organisationId, String reason, Map<String,Object> details) {
        if (!java.util.Set.of("USER", "ADMIN", "SERVICE").contains(kind)) {
            throw new IllegalArgumentException("Unknown audit actor kind");
        }
        // Only numeric amounts and identifiers belong in details. No free-form credentials or PII.
        details.forEach((key,value) -> {
            if (!(value instanceof UUID || value instanceof Number)) {
                throw new IllegalArgumentException("Audit details may contain only identifiers and amounts");
            }
        });
        var event = new AuditEvent(now, actor, kind, action, subjectType, subjectId, organisationId, reason, details);
        entityManager.persist(event);
        return event.getId();
    }
}
