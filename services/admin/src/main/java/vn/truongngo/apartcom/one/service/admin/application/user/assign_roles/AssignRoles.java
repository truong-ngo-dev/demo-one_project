package vn.truongngo.apartcom.one.service.admin.application.user.assign_roles;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleException;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AssignRoles {

    public record Command(String userId, List<String> roleIds) {
        public Command {
            Assert.hasText(userId, "userId is required");
            Assert.notNull(roleIds, "roleIds is required");
            Assert.isTrue(!roleIds.isEmpty(), "at least one roleId is required");
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

            Set<RoleId> roleIds = command.roleIds().stream()
                    .map(RoleId::of)
                    .collect(Collectors.toSet());
            if (roleRepository.findAllByIds(roleIds).size() != command.roleIds().size()) {
                throw RoleException.notFound();
            }

            boolean hasAdminContext = user.getRoleContexts().stream()
                    .anyMatch(ctx -> ctx.matchesScope(Scope.ADMIN, null));
            if (!hasAdminContext) {
                user.addRoleContext(Scope.ADMIN, null, roleIds);
            } else {
                for (RoleId roleId : roleIds) {
                    user.assignRoleToContext(Scope.ADMIN, null, roleId);
                }
            }
            userRepository.save(user);

            return null;
        }
    }
}
