package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.tenant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRole;

import java.time.Instant;

@Entity
@Table(name = "tenant_sub_role_assignment")
@Getter
@Setter
@NoArgsConstructor
public class TenantSubRoleAssignmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "org_id", nullable = false, length = 36)
    private String orgId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_role", nullable = false)
    private TenantSubRole subRole;

    @Column(name = "assigned_by", nullable = false, length = 36)
    private String assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;
}
