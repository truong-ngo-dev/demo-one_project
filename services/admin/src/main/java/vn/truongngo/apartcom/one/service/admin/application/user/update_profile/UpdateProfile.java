package vn.truongngo.apartcom.one.service.admin.application.user.update_profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.util.Optional;

public class UpdateProfile {

    public record Command(String userId, String username, String fullName, String phoneNumber) {
        public Command {
            Assert.hasText(userId, "userId is required");
            Assert.isTrue(
                    username != null || fullName != null || phoneNumber != null,
                    "at least one field is required"
            );
        }
    }

    public record Result(String id, String username, String fullName, String phoneNumber) {}

    static class Mapper {
        static Result toResult(User user) {
            return new Result(
                    user.getId().getValue(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getPhoneNumber()
            );
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final UserRepository userRepository;

        @Override
        @Transactional
        public Result handle(Command command) {
            User user = userRepository.findById(UserId.of(command.userId()))
                    .orElseThrow(UserException::notFound);

            if (command.username() != null) {
                Optional<User> existing = userRepository.findByUsername(command.username());
                if (existing.isPresent() && !existing.get().getId().getValue().equals(user.getId().getValue())) {
                    throw UserException.usernameAlreadyExists();
                }
            }

            if (command.phoneNumber() != null) {
                Optional<User> existing = userRepository.findByPhoneNumber(command.phoneNumber());
                if (existing.isPresent() && !existing.get().getId().getValue().equals(user.getId().getValue())) {
                    throw UserException.phoneAlreadyExists();
                }
            }

            user.updateProfile(command.username(), command.fullName(), command.phoneNumber());
            userRepository.save(user);
            return Mapper.toResult(user);
        }
    }
}
