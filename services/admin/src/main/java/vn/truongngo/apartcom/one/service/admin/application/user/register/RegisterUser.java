package vn.truongngo.apartcom.one.service.admin.application.user.register;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserFactory;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserPassword;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.Set;

public class RegisterUser {

    public record Command(String email, String username, String password, String fullName) {
        public Command {
            Assert.hasText(email, "email is required");
            Assert.hasText(username, "username is required");
            Assert.hasText(password, "password is required");
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

        private final UserRepository userRepository;
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

            UserPassword password = UserPassword.ofHashed(passwordEncoder.encode(command.password()));

            User user = UserFactory.register(
                    command.username(),
                    command.email(),
                    null,
                    command.fullName(),
                    password,
                    Set.of()
            );

            userRepository.save(user);
            eventDispatcher.dispatchAll(user.pullDomainEvents());

            return Mapper.toResult(user);
        }
    }
}
