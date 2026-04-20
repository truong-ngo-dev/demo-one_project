package vn.truongngo.apartcom.one.service.admin.domain.tenant;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;

import java.time.Instant;

@Getter
public class TenantSubRoleAssignment extends AbstractAggregateRoot<TenantSubRoleAssignmentId>
        implements AggregateRoot<TenantSubRoleAssignmentId> {

    private final String userId;
    private final String orgId;
    private final TenantSubRole subRole;
    private final String assignedBy;
    private final Instant assignedAt;

    private TenantSubRoleAssignment(TenantSubRoleAssignmentId id, String userId, String orgId,
                                    TenantSubRole subRole, String assignedBy, Instant assignedAt) {
        super(id);
        this.userId     = userId;
        this.orgId      = orgId;
        this.subRole    = subRole;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
    }

    public static TenantSubRoleAssignment create(String userId, String orgId,
                                                 TenantSubRole subRole, String assignedBy) {
        return new TenantSubRoleAssignment(
                TenantSubRoleAssignmentId.generate(),
                userId, orgId, subRole, assignedBy,
                Instant.now()
        );
    }

    public static TenantSubRoleAssignment reconstitute(TenantSubRoleAssignmentId id, String userId,
                                                       String orgId, TenantSubRole subRole,
                                                       String assignedBy, Instant assignedAt) {
        return new TenantSubRoleAssignment(id, userId, orgId, subRole, assignedBy, assignedAt);
    }
}
