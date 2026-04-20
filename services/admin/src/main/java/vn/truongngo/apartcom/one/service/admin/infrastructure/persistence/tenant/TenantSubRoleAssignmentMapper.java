package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.tenant;

import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignment;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignmentId;

public class TenantSubRoleAssignmentMapper {

    public static TenantSubRoleAssignment toDomain(TenantSubRoleAssignmentJpaEntity entity) {
        return TenantSubRoleAssignment.reconstitute(
                TenantSubRoleAssignmentId.of(entity.getId()),
                entity.getUserId(),
                entity.getOrgId(),
                entity.getSubRole(),
                entity.getAssignedBy(),
                entity.getAssignedAt()
        );
    }

    public static TenantSubRoleAssignmentJpaEntity toEntity(TenantSubRoleAssignment domain) {
        TenantSubRoleAssignmentJpaEntity entity = new TenantSubRoleAssignmentJpaEntity();
        entity.setId(domain.getId().getValue());
        entity.setUserId(domain.getUserId());
        entity.setOrgId(domain.getOrgId());
        entity.setSubRole(domain.getSubRole());
        entity.setAssignedBy(domain.getAssignedBy());
        entity.setAssignedAt(domain.getAssignedAt());
        return entity;
    }
}
