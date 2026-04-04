package vn.truongngo.apartcom.one.service.admin.application.role.find_all;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.domain.role.Role;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;

public class FindAllRoles {

    public record Query(String keyword, int page, int size, String sort) {

        public static Query of(String keyword, Integer page, Integer size, String sort) {
            return new Query(
                    keyword,
                    page != null ? Math.max(page, 0) : 0,
                    size != null ? Math.min(Math.max(size, 1), 100) : 20,
                    sort != null ? sort : "name,asc"
            );
        }
    }

    public record RoleSummary(String id, String name, String description) {}

    static class Mapper {
        static RoleSummary toSummary(Role role) {
            return new RoleSummary(role.getId().getValue(), role.getName(), role.getDescription());
        }

        static Pageable toPageable(Query query) {
            String[] parts = query.sort().split(",");
            String field = parts[0].trim();
            Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            return PageRequest.of(query.page(), query.size(), Sort.by(direction, field));
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Page<RoleSummary>> {

        private final RoleRepository roleRepository;

        @Override
        public Page<RoleSummary> handle(Query query) {
            Pageable pageable = Mapper.toPageable(query);
            return roleRepository.findAll(query.keyword(), pageable)
                    .map(Mapper::toSummary);
        }
    }
}
