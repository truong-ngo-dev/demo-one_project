package vn.truongngo.apartcom.one.service.admin.domain.abac.audit;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published by command handlers after any ABAC entity mutation.
 * Handled by {@code AuditLogEventHandler} which persists an audit log entry.
 */
@Getter
public class AbacAuditLogEvent extends AbstractDomainEvent {

    private final AuditEntityType entityType;
    private final Long entityId;
    private final String entityName;
    private final AuditActionType actionType;
    private final String performedBy;
    private final String snapshotJson;

    public AbacAuditLogEvent(AuditEntityType entityType, Long entityId, String entityName,
                              AuditActionType actionType, String performedBy, String snapshotJson) {
        super(UUID.randomUUID().toString(), String.valueOf(entityId), Instant.now());
        this.entityType   = entityType;
        this.entityId     = entityId;
        this.entityName   = entityName;
        this.actionType   = actionType;
        this.performedBy  = performedBy;
        this.snapshotJson = snapshotJson;
    }
}
