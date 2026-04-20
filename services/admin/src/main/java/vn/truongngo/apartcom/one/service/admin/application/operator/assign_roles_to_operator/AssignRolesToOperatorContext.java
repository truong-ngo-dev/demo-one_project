package vn.truongngo.apartcom.one.service.admin.application.operator.assign_roles_to_operator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleException;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContext;
import vn.truongngo.apartcom.one.service.admin.domain.user.RoleContextStatus;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserErrorCode;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AssignRolesToOperatorContext {

    public record Command(String userId, String buildingId, List<String> roleIds) {
        public Command {
            Assert.hasText(userId, "userId is required");
            Assert.hasText(buildingId, "buildingId is required");
            Assert.notNull(roleIds, "roleIds is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;

        @Override
        @Transactional
        public Void handle(Command command) {
            User user = userRepository.findById(UserId.of(command.userId()))
                    .orElseThrow(UserException::notFound);

            // Find existing OPERATOR context for this building
            RoleContext ctx = user.getRoleContexts().stream()
                    .filter(c -> c.matchesScope(Scope.OPERATOR, command.buildingId()))
                    .findFirst()
                    .orElseThrow(() -> new DomainException(UserErrorCode.ROLE_CONTEXT_NOT_FOUND));

            if (ctx.getStatus() == RoleContextStatus.REVOKED) {
                throw new DomainException(UserErrorCode.ROLE_CONTEXT_ALREADY_REVOKED);
            }

            Set<RoleId> newRoleIds = command.roleIds().stream()
                    .map(RoleId::of)
                    .collect(Collectors.toSet());

            // B6: all roles must exist and have scope == OPERATOR
            var roles = roleRepository.findAllByIds(newRoleIds);
            if (roles.size() != newRoleIds.size()) {
                throw RoleException.notFound();
            }
            roles.forEach(role -> {
                if (role.getScope() != Scope.OPERATOR) {
                    throw new DomainException(UserErrorCode.ROLE_SCOPE_MISMATCH);
                }
            });

            // Replace: remove old roles, add new roles
            Set<RoleId> currentRoleIds = user.getRoleIdsForScope(Scope.OPERATOR, command.buildingId());
            currentRoleIds.forEach(oldId -> user.removeRoleFromContext(Scope.OPERATOR, command.buildingId(), oldId));
            newRoleIds.forEach(newId -> user.assignRoleToContext(Scope.OPERATOR, command.buildingId(), newId));

            userRepository.save(user);

            return null;
        }
    }
}
