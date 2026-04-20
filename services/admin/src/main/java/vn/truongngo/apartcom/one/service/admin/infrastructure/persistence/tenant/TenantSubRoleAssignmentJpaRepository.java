package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRole;

import java.util.List;

public interface TenantSubRoleAssignmentJpaRepository
        extends JpaRepository<TenantSubRoleAssignmentJpaEntity, String> {

    boolean existsByUserIdAndOrgIdAndSubRole(String userId, String orgId, TenantSubRole subRole);

    List<TenantSubRoleAssignmentJpaEntity> findAllByOrgId(String orgId);

    void deleteAllByOrgId(String orgId);
}
