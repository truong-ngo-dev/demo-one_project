package vn.truongngo.apartcom.one.service.admin.application.tenant.assign_sub_role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRole;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignment;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleAssignmentRepository;
import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRoleException;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContextStatus;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

public class AssignSubRole {

    public record Command(String userId, String orgId, TenantSubRole subRole, String assignedBy) {
        public Command {
            Assert.hasText(userId, "userId is required");
            Assert.hasText(orgId, "orgId is required");
            Assert.notNull(subRole, "subRole is required");
            Assert.hasText(assignedBy, "assignedBy is required");
        }
    }

    public record Result(String assignmentId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final UserRepository userRepository;
        private final TenantSubRoleAssignmentRepository subRoleRepository;

        @Override
        @Transactional
        public Result handle(Command command) {
            // B7: assigner must have active TENANT context for orgId
            User assigner = userRepository.findById(UserId.of(command.assignedBy()))
                    .orElseThrow(UserException::notFound);
            boolean assignerAuthorized = assigner.getRoleContexts().stream()
                    .anyMatch(ctx -> ctx.matchesScope(Scope.TENANT, command.orgId())
                            && ctx.getStatus() == RoleContextStatus.ACTIVE);
            if (!assignerAuthorized) {
                throw TenantSubRoleException.assignerNotAuthorized();
            }

            // B8: target user must have active TENANT context for orgId
            User targetUser = userRepository.findById(UserId.of(command.userId()))
                    .orElseThrow(UserException::notFound);
            boolean targetIsMember = targetUser.getRoleContexts().stream()
                    .anyMatch(ctx -> ctx.matchesScope(Scope.TENANT, command.orgId())
                            && ctx.getStatus() == RoleContextStatus.ACTIVE);
            if (!targetIsMember) {
                throw TenantSubRoleException.targetUserNotTenantMember();
            }

            // Duplicate check
            if (subRoleRepository.existsByUserIdAndOrgIdAndSubRole(
                    command.userId(), command.orgId(), command.subRole())) {
                throw TenantSubRoleException.alreadyAssigned();
            }

            TenantSubRoleAssignment assignment = TenantSubRoleAssignment.create(
                    command.userId(), command.orgId(), command.subRole(), command.assignedBy());
            TenantSubRoleAssignment saved = subRoleRepository.save(assignment);

            return new Result(saved.getId().getValue());
        }
    }
}
