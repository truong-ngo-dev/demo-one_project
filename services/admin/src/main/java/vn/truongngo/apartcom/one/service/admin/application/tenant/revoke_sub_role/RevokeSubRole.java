package vn.truongngo.apartcom.one.service.admin.application.tenant.revoke_sub_role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRole;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignment;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignmentRepository;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleException;

public class RevokeSubRole {

    public record Command(String userId, String orgId, TenantSubRole subRole) {
        public Command {
            Assert.hasText(userId, "userId is required");
            Assert.hasText(orgId, "orgId is required");
            Assert.notNull(subRole, "subRole is required");
        }
    }

    public record Result() {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final TenantSubRoleAssignmentRepository subRoleRepository;

        @Override
        @Transactional
        public Result handle(Command command) {
            TenantSubRoleAssignment assignment = subRoleRepository.findByOrgId(command.orgId())
                    .stream()
                    .filter(a -> a.getUserId().equals(command.userId())
                            && a.getSubRole() == command.subRole())
                    .findFirst()
                    .orElseThrow(TenantSubRoleException::notFound);

            subRoleRepository.delete(assignment.getId());
            return new Result();
        }
    }
}
