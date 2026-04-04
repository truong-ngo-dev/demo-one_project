package vn.truongngo.apartcom.one.service.admin.application.user.admin_create;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleException;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserFactory;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserPassword;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminCreateUser {

    public record Command(String email, String username, String fullName, List<String> roleIds) {
        public Command {
            Assert.hasText(email, "email is required");
            Assert.hasText(username, "username is required");
            Assert.notNull(roleIds, "roleIds is required");
            Assert.isTrue(!roleIds.isEmpty(), "at least one role is required");
        }
    }

    public record Result(String id, String username) {}

    static class Mapper {
        static Result toResult(User user) {
            return new Result(user.getId().getValue(), user.getUsername());
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private static final String DEFAULT_PASSWORD = "Admin@123456";

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            if (userRepository.existsByEmail(command.email())) {
                throw UserException.emailAlreadyExists();
            }
            if (userRepository.existsByUsername(command.username())) {
                throw UserException.usernameAlreadyExists();
            }

            Set<RoleId> roleIds = command.roleIds().stream()
                    .map(RoleId::of)
                    .collect(Collectors.toSet());
            if (roleRepository.findAllByIds(roleIds).size() != command.roleIds().size()) {
                throw RoleException.notFound();
            }

            UserPassword password = UserPassword.ofHashed(passwordEncoder.encode(DEFAULT_PASSWORD));

            User user = UserFactory.adminCreate(
                    command.username(),
                    command.email(),
                    command.fullName(),
                    password,
                    roleIds
            );

            userRepository.save(user);
            eventDispatcher.dispatchAll(user.pullDomainEvents());

            return Mapper.toResult(user);
        }
    }
}
