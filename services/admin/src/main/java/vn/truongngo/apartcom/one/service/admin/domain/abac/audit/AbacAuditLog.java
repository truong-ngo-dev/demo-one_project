package vn.truongngo.apartcom.one.service.admin.domain.abac.audit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AbacAuditLog {

    private Long id;
    private AuditEntityType entityType;
    private Long entityId;
    private String entityName;
    private AuditActionType actionType;
    private String performedBy;
    private Long changedAt;
    private String snapshotJson;
}
