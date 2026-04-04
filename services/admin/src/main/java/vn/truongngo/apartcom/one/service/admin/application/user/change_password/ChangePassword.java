package vn.truongngo.apartcom.one.service.admin.application.user.change_password;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserPassword;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

public class ChangePassword {

    public record Command(String userId, String currentPassword, String newPassword) {
        public Command {
            Assert.hasText(userId, "userId is required");
            Assert.hasText(newPassword, "newPassword is required");
        }
    }

    public record Result(boolean changed, String message) {
        public static Result success()   { return new Result(true, null); }
        public static Result unchanged() { return new Result(false, "New password is the same as current password"); }
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
            User user = userRepository.findById(UserId.of(command.userId()))
                    .orElseThrow(UserException::notFound);

            if (user.hasPassword()) {
                if (command.currentPassword() == null) {
                    throw UserException.currentPasswordRequired();
                }
                if (!passwordEncoder.matches(command.currentPassword(), user.getPassword().getHashedValue())) {
                    throw UserException.invalidPassword();
                }
                if (passwordEncoder.matches(command.newPassword(), user.getPassword().getHashedValue())) {
                    return Result.unchanged();
                }
            }

            UserPassword newPassword = UserPassword.ofHashed(passwordEncoder.encode(command.newPassword()));
            user.changePassword(newPassword);
            userRepository.save(user);
            eventDispatcher.dispatchAll(user.pullDomainEvents());
            return Result.success();
        }
    }
}
