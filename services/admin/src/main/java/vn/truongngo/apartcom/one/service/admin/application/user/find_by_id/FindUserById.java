package vn.truongngo.apartcom.one.service.admin.application.user.find_by_id;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.role.Role;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserException;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserId;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public class FindUserById {

    public record Query(String id) {
        public Query {
            Assert.hasText(id, "id is required");
        }
    }

    public record RoleView(String id, String name) {}

    public record SocialConnectionView(String provider, Long connectedAt) {}

    public record UserDetail(
            String id,
            String email,
            String username,
            String fullName,
            String status,
            List<RoleView> roles,
            List<SocialConnectionView> socialConnections,
            Instant createdAt
    ) {}

    static class Mapper {
        static UserDetail toDetail(User user, Set<Role> roles) {
            List<RoleView> roleViews = roles.stream()
                    .map(r -> new RoleView(r.getId().getValue(), r.getName()))
                    .toList();

            List<SocialConnectionView> socialConnections = user.getSocialConnections().stream()
                    .map(s -> new SocialConnectionView(s.getProvider(), s.getConnectedAt().toEpochMilli()))
                    .toList();

            return new UserDetail(
                    user.getId().getValue(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getStatus().name(),
                    roleViews,
                    socialConnections,
                    user.getCreatedAt()
            );
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, UserDetail> {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;

        @Override
        public UserDetail handle(Query query) {
            User user = userRepository.findById(UserId.of(query.id()))
                    .orElseThrow(UserException::notFound);
            Set<Role> roles = roleRepository.findAllByIds(user.getRoleIdsForScope(Scope.ADMIN, null));
            return Mapper.toDetail(user, roles);
        }
    }
}
