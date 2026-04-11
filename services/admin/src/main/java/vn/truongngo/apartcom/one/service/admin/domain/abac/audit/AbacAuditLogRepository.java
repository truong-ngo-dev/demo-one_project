package vn.truongngo.apartcom.one.service.admin.domain.abac.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbacAuditLogRepository extends JpaRepository<AbacAuditLog, Long> {

    Page<AbacAuditLog> findAllByOrderByChangedAtDesc(Pageable pageable);

    Page<AbacAuditLog> findByEntityTypeOrderByChangedAtDesc(AuditEntityType entityType, Pageable pageable);

    Page<AbacAuditLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(AuditEntityType entityType, Long entityId, Pageable pageable);

    Page<AbacAuditLog> findByPerformedByOrderByChangedAtDesc(String performedBy, Pageable pageable);
}
