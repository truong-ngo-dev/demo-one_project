package vn.truongngo.apartcom.one.service.admin.application.audit.list_audit_log;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLog;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditActionType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;

public class ListAuditLog {

    public record Query(
            AuditEntityType entityType,  // null = all
            Long entityId,               // null = all
            String performedBy,          // null = all
            int page,
            int size
    ) {}

    public record AuditLogEntry(
            Long id,
            String entityType,
            Long entityId,
            String entityName,
            String actionType,
            String performedBy,
            Long changedAt,
            String snapshotJson
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Page<AuditLogEntry>> {

        private final AbacAuditLogRepository repository;

        @Override
        public Page<AuditLogEntry> handle(Query query) {
            Pageable pageable = PageRequest.of(query.page(), query.size());

            Page<AbacAuditLog> page;
            if (query.entityType() != null && query.entityId() != null) {
                page = repository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(
                        query.entityType(), query.entityId(), pageable);
            } else if (query.entityType() != null) {
                page = repository.findByEntityTypeOrderByChangedAtDesc(query.entityType(), pageable);
            } else if (query.performedBy() != null && !query.performedBy().isBlank()) {
                page = repository.findByPerformedByOrderByChangedAtDesc(query.performedBy(), pageable);
            } else {
                page = repository.findAllByOrderByChangedAtDesc(pageable);
            }

            return page.map(log -> new AuditLogEntry(
                    log.getId(),
                    log.getEntityType().name(),
                    log.getEntityId(),
                    log.getEntityName(),
                    log.getActionType().name(),
                    log.getPerformedBy(),
                    log.getChangedAt(),
                    log.getSnapshotJson()
            ));
        }
    }
}
