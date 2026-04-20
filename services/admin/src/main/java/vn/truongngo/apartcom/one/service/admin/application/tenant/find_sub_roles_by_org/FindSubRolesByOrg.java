package vn.truongngo.apartcom.one.service.admin.application.tenant.find_sub_roles_by_org;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRole;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignment;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignmentRepository;

import java.time.Instant;
import java.util.List;

public class FindSubRolesByOrg {

    public record Query(String orgId) {
        public Query {
            Assert.hasText(orgId, "orgId is required");
        }
    }

    public record SubRoleView(
            String assignmentId,
            String userId,
            String orgId,
            TenantSubRole subRole,
            String assignedBy,
            Instant assignedAt
    ) {
        public static SubRoleView from(TenantSubRoleAssignment assignment) {
            return new SubRoleView(
                    assignment.getId().getValue(),
                    assignment.getUserId(),
                    assignment.getOrgId(),
                    assignment.getSubRole(),
                    assignment.getAssignedBy(),
                    assignment.getAssignedAt()
            );
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<SubRoleView>> {

        private final TenantSubRoleAssignmentRepository subRoleRepository;

        @Override
        public List<SubRoleView> handle(Query query) {
            return subRoleRepository.findByOrgId(query.orgId()).stream()
                    .map(SubRoleView::from)
                    .toList();
        }
    }
}
