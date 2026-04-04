package vn.truongngo.apartcom.one.service.admin.application.role.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleException;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

public class DeleteRole {

    public record Command(String id) {
        public Command {
            Assert.hasText(id, "id is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final RoleRepository roleRepository;
        private final UserRepository userRepository;

        @Override
        @Transactional
        public Void handle(Command command) {
            RoleId roleId = RoleId.of(command.id());

            roleRepository.findById(roleId)
                    .orElseThrow(RoleException::notFound);

            if (userRepository.existsByRoleId(roleId)) {
                throw RoleException.inUse();
            }

            roleRepository.delete(roleId);

            return null;
        }
    }
}
