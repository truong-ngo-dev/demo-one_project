package vn.truongngo.apartcom.one.service.admin.application.user.social_register;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserFactory;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.time.Instant;
import java.util.Optional;

public class SocialRegisterUser {

    public record Command(String provider, String providerUserId, String providerEmail) {
        public Command {
            Assert.hasText(provider, "provider is required");
            Assert.hasText(providerUserId, "providerUserId is required");
            Assert.hasText(providerEmail, "providerEmail is required");
        }
    }

    public record Result(String userId, String username, boolean requiresProfileCompletion) {}

    static class Mapper {
        static Result toResult(User user, boolean requiresProfileCompletion) {
            return new Result(user.getId().getValue(), user.getUsername(), requiresProfileCompletion);
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final UserRepository userRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            Optional<User> existing = userRepository.findByEmail(command.providerEmail());

            if (existing.isPresent()) {
                User user = existing.get();
                if (!user.hasSocialConnection(command.provider(), command.providerUserId())) {
                    user.connectSocial(command.provider(), command.providerUserId(), command.providerEmail(), Instant.now());
                    userRepository.save(user);
                    eventDispatcher.dispatchAll(user.pullDomainEvents());
                }
                return Mapper.toResult(user, false);
            }

            String emailPrefix = command.providerEmail().split("@")[0];
            String username = emailPrefix + "_" + Instant.now().toEpochMilli();

            User user = UserFactory.socialRegister(
                    username,
                    command.providerEmail(),
                    command.provider(),
                    command.providerUserId(),
                    command.providerEmail(),
                    Instant.now()
            );

            userRepository.save(user);
            eventDispatcher.dispatchAll(user.pullDomainEvents());

            return Mapper.toResult(user, true);
        }
    }
}
