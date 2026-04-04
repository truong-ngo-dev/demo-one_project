package vn.truongngo.apartcom.one.service.admin.application.user.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.user.User;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserRepository;
import vn.truongngo.apartcom.one.service.admin.domain.user.UserStatus;

public class SearchUsers {

    public record Query(String keyword, String status, String roleId, int page, int size, String sort) {

        public static Query of(String keyword, String status, String roleId, Integer page, Integer size, String sort) {
            return new Query(
                    keyword,
                    status,
                    roleId,
                    page != null ? page : 0,
                    size != null ? Math.min(size, 100) : 20,
                    sort != null ? sort : "createdAt,desc"
            );
        }
    }

    public record UserSummary(String id, String email, String username, String fullName, String status) {}

    static class Mapper {
        static UserSummary toSummary(User user) {
            return new UserSummary(
                    user.getId().getValue(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getStatus().name()
            );
        }

        static Pageable toPageable(Query query) {
            String[] parts = query.sort().split(",");
            String field = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            return PageRequest.of(query.page(), query.size(), Sort.by(direction, field));
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Page<UserSummary>> {

        private final UserRepository userRepository;

        @Override
        public Page<UserSummary> handle(Query query) {
            UserStatus status = query.status() != null ? UserStatus.valueOf(query.status()) : null;
            RoleId roleId = query.roleId() != null ? RoleId.of(query.roleId()) : null;
            Pageable pageable = Mapper.toPageable(query);

            return userRepository.findAll(query.keyword(), status, roleId, pageable)
                    .map(Mapper::toSummary);
        }
    }
}
