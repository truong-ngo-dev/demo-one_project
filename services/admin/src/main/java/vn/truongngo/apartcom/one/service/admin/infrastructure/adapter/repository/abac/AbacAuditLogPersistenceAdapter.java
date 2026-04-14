package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLog;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.audit.AbacAuditLogJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.audit.AbacAuditLogJpaRepository;

@Component
@RequiredArgsConstructor
public class AbacAuditLogPersistenceAdapter implements AbacAuditLogRepository {

    private final AbacAuditLogJpaRepository jpaRepository;

    @Override
    public AbacAuditLog save(AbacAuditLog log) {
        AbacAuditLogJpaEntity entity = toEntity(log);
        AbacAuditLogJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Page<AbacAuditLog> findAll(Pageable pageable) {
        return jpaRepository.findAllByOrderByChangedAtDesc(pageable).map(this::toDomain);
    }

    @Override
    public Page<AbacAuditLog> findByEntityType(AuditEntityType entityType, Pageable pageable) {
        return jpaRepository.findByEntityTypeOrderByChangedAtDesc(entityType, pageable).map(this::toDomain);
    }

    @Override
    public Page<AbacAuditLog> findByEntityTypeAndEntityId(AuditEntityType entityType, Long entityId, Pageable pageable) {
        return jpaRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId, pageable).map(this::toDomain);
    }

    @Override
    public Page<AbacAuditLog> findByPerformedBy(String performedBy, Pageable pageable) {
        return jpaRepository.findByPerformedByOrderByChangedAtDesc(performedBy, pageable).map(this::toDomain);
    }

    private AbacAuditLog toDomain(AbacAuditLogJpaEntity entity) {
        AbacAuditLog log = new AbacAuditLog();
        log.setId(entity.getId());
        log.setEntityType(entity.getEntityType());
        log.setEntityId(entity.getEntityId());
        log.setEntityName(entity.getEntityName());
        log.setActionType(entity.getActionType());
        log.setPerformedBy(entity.getPerformedBy());
        log.setChangedAt(entity.getChangedAt());
        log.setSnapshotJson(entity.getSnapshotJson());
        return log;
    }

    private AbacAuditLogJpaEntity toEntity(AbacAuditLog log) {
        AbacAuditLogJpaEntity entity = new AbacAuditLogJpaEntity();
        entity.setId(log.getId());
        entity.setEntityType(log.getEntityType());
        entity.setEntityId(log.getEntityId());
        entity.setEntityName(log.getEntityName());
        entity.setActionType(log.getActionType());
        entity.setPerformedBy(log.getPerformedBy());
        entity.setChangedAt(log.getChangedAt());
        entity.setSnapshotJson(log.getSnapshotJson());
        return entity;
    }
}
