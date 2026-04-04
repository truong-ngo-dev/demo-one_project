package vn.truongngo.apartcom.one.service.admin.application.user.get_my_profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

public class GetMyProfile {

    public record Query(String userId) {
        public Query {
            Assert.hasText(userId, "userId is required");
        }
    }

    public record Result(
            String id,
            String email,
            String username,
            String fullName,
            String phoneNumber,
            boolean usernameChanged,
            boolean hasPassword
    ) {}

    static class Mapper {
        static Result toResult(User user) {
            return new Result(
                    user.getId().getValue(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.isUsernameChanged(),
                    user.hasPassword()
            );
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final UserRepository userRepository;

        @Override
        public Result handle(Query query) {
            User user = userRepository.findById(UserId.of(query.userId()))
                    .orElseThrow(UserException::notFound);
            return Mapper.toResult(user);
        }
    }
}
