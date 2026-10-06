package com.fooddelivery.common.audit;

import com.fooddelivery.common.enums.AuditAction;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
@Immutable
@Getter
@NoArgsConstructor
public class AuditEvent {
    @Id private UUID id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_kind", nullable = false, length = 20)
    private String actorKind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private AuditAction action;

    @Column(name = "subject_type", nullable = false, length = 40)
    private String subjectType;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(length = 500)
    private String reason;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    public AuditEvent(
            Instant now,
            UUID actor,
            String kind,
            AuditAction action,
            String subjectType,
            UUID subjectId,
            UUID organisationId,
            String reason,
            Map<String, Object> details) {
        this.id = UUID.randomUUID();
        this.occurredAt = now;
        this.actorUserId = actor;
        this.actorKind = kind;
        this.action = action;
        this.subjectType = subjectType;
        this.subjectId = subjectId;
        this.organisationId = organisationId;
        this.reason = reason;
        this.traceId = org.slf4j.MDC.get("traceId");
        this.details = Map.copyOf(details);
    }
}
