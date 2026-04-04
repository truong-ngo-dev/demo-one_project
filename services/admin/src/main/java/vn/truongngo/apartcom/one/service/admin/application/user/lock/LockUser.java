package vn.truongngo.apartcom.one.service.admin.application.user.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

public class LockUser {

    public record Command(String id) {
        public Command {
            Assert.hasText(id, "id is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final UserRepository userRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Void handle(Command command) {
            User user = userRepository.findById(UserId.of(command.id()))
                    .orElseThrow(UserException::notFound);

            if (!user.isActive()) {
                throw UserException.invalidStatus();
            }

            user.lock();
            userRepository.save(user);
            eventDispatcher.dispatchAll(user.pullDomainEvents());

            return null;
        }
    }
}
