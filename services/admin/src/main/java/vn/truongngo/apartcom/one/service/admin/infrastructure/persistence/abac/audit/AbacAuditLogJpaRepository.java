package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditActionType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;

public interface AbacAuditLogJpaRepository extends JpaRepository<AbacAuditLogJpaEntity, Long> {

    Page<AbacAuditLogJpaEntity> findAllByOrderByChangedAtDesc(Pageable pageable);

    Page<AbacAuditLogJpaEntity> findByEntityTypeOrderByChangedAtDesc(AuditEntityType entityType, Pageable pageable);

    Page<AbacAuditLogJpaEntity> findByEntityTypeAndEntityIdOrderByChangedAtDesc(AuditEntityType entityType, Long entityId, Pageable pageable);

    Page<AbacAuditLogJpaEntity> findByPerformedByOrderByChangedAtDesc(String performedBy, Pageable pageable);
}
