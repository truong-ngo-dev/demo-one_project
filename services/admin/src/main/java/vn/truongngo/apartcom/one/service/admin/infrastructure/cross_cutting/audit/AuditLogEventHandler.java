package vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.domain.service.EventHandler;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLog;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogEvent;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogRepository;

@Component
@RequiredArgsConstructor
public class AuditLogEventHandler implements EventHandler<AbacAuditLogEvent> {

    private final AbacAuditLogRepository repository;

    @Override
    public void handle(AbacAuditLogEvent event) {
        AbacAuditLog log = new AbacAuditLog();
        log.setEntityType(event.getEntityType());
        log.setEntityId(event.getEntityId());
        log.setEntityName(event.getEntityName());
        log.setActionType(event.getActionType());
        log.setPerformedBy(event.getPerformedBy());
        log.setChangedAt(System.currentTimeMillis());
        log.setSnapshotJson(event.getSnapshotJson());
        repository.save(log);
    }

    @Override
    public Class<AbacAuditLogEvent> getEventType() {
        return AbacAuditLogEvent.class;
    }
}
