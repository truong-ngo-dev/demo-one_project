package vn.truongngo.apartcom.one.service.admin.application.role.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.role.Role;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleException;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;

public class CreateRole {

    public record Command(String name, String description, Scope scope) {
        public Command {
            Assert.hasText(name, "name is required");
            Assert.notNull(scope, "scope is required");
        }
    }

    public record Result(String id) {}

    static class Mapper {
        static Result toResult(Role role) {
            return new Result(role.getId().getValue());
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final RoleRepository roleRepository;

        @Override
        @Transactional
        public Result handle(Command command) {
            if (roleRepository.existsByName(command.name())) {
                throw RoleException.alreadyExists();
            }

            Role role = Role.register(command.name(), command.description(), command.scope());
            roleRepository.save(role);

            return Mapper.toResult(role);
        }
    }
}
