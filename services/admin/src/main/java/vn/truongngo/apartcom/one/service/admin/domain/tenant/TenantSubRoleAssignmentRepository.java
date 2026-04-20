package vn.truongngo.apartcom.one.service.admin.domain.tenant;

import java.util.List;

public interface TenantSubRoleAssignmentRepository {

    TenantSubRoleAssignment save(TenantSubRoleAssignment assignment);

    void delete(TenantSubRoleAssignmentId id);

    List<TenantSubRoleAssignment> findByOrgId(String orgId);

    boolean existsByUserIdAndOrgIdAndSubRole(String userId, String orgId, TenantSubRole subRole);

    void deleteAllByOrgId(String orgId);
}
