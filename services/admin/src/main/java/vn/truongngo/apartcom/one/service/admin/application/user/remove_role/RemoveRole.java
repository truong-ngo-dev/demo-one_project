package vn.truongngo.apartcom.one.service.admin.application.user.remove_role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleException;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

public class RemoveRole {

    public record Command(String userId, String roleId) {
        public Command {
            Assert.hasText(userId, "userId is required");
            Assert.hasText(roleId, "roleId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final UserRepository userRepository;

        @Override
        @Transactional
        public Void handle(Command command) {
            User user = userRepository.findById(UserId.of(command.userId()))
                    .orElseThrow(UserException::notFound);

            RoleId roleId = RoleId.of(command.roleId());
            boolean hasRole = user.getRoleIdsForScope(Scope.ADMIN, null).stream()
                    .anyMatch(r -> r.getValue().equals(roleId.getValue()));
            if (!hasRole) {
                throw RoleException.notFound();
            }

            user.removeRoleFromContext(Scope.ADMIN, null, roleId);
            userRepository.save(user);

            return null;
        }
    }
}
