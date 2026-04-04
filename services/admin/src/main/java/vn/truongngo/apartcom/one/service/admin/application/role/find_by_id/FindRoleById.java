package vn.truongngo.apartcom.one.service.admin.application.role.find_by_id;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.role.Role;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleException;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleRepository;

import java.time.Instant;

public class FindRoleById {

    public record Query(String id) {
        public Query {
            Assert.hasText(id, "id is required");
        }
    }

    public record RoleDetail(String id, String name, String description, Instant createdAt) {}

    static class Mapper {
        static RoleDetail toDetail(Role role) {
            Instant createdAt = role.getAuditable() != null && role.getAuditable().getCreatedAt() != null
                    ? Instant.ofEpochMilli(role.getAuditable().getCreatedAt())
                    : null;
            return new RoleDetail(role.getId().getValue(), role.getName(), role.getDescription(), createdAt);
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, RoleDetail> {

        private final RoleRepository roleRepository;

        @Override
        public RoleDetail  handle(Query query) {
            return roleRepository.findById(RoleId.of(query.id()))
                    .map(Mapper::toDetail)
                    .orElseThrow(RoleException::notFound);
        }
    }
}
