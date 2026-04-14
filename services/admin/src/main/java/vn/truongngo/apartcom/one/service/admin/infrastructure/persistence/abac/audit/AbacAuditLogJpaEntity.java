package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditActionType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;

@Entity
@Table(name = "abac_audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AbacAuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private AuditActionType actionType;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "changed_at", nullable = false)
    private Long changedAt;

    @Column(name = "snapshot_json", columnDefinition = "TEXT")
    private String snapshotJson;
}
