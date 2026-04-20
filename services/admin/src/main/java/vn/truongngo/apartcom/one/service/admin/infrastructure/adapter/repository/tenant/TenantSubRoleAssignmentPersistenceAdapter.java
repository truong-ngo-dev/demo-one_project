package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRole;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignment;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignmentId;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignmentRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.tenant.TenantSubRoleAssignmentJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.tenant.TenantSubRoleAssignmentMapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TenantSubRoleAssignmentPersistenceAdapter implements TenantSubRoleAssignmentRepository {

    private final TenantSubRoleAssignmentJpaRepository jpaRepository;

    @Override
    public TenantSubRoleAssignment save(TenantSubRoleAssignment assignment) {
        return TenantSubRoleAssignmentMapper.toDomain(
                jpaRepository.save(TenantSubRoleAssignmentMapper.toEntity(assignment))
        );
    }

    @Override
    public void delete(TenantSubRoleAssignmentId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public List<TenantSubRoleAssignment> findByOrgId(String orgId) {
        return jpaRepository.findAllByOrgId(orgId).stream()
                .map(TenantSubRoleAssignmentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndOrgIdAndSubRole(String userId, String orgId, TenantSubRole subRole) {
        return jpaRepository.existsByUserIdAndOrgIdAndSubRole(userId, orgId, subRole);
    }

    @Override
    public void deleteAllByOrgId(String orgId) {
        jpaRepository.deleteAllByOrgId(orgId);
    }
}
