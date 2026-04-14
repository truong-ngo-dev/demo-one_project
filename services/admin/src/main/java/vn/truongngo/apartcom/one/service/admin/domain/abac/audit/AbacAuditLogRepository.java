package vn.truongngo.apartcom.one.service.admin.domain.abac.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AbacAuditLogRepository {

    AbacAuditLog save(AbacAuditLog log);

    Page<AbacAuditLog> findAll(Pageable pageable);

    Page<AbacAuditLog> findByEntityType(AuditEntityType entityType, Pageable pageable);

    Page<AbacAuditLog> findByEntityTypeAndEntityId(AuditEntityType entityType, Long entityId, Pageable pageable);

    Page<AbacAuditLog> findByPerformedBy(String performedBy, Pageable pageable);
}
